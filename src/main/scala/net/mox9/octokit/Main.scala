package net.mox9.octokit

import play.api._

object Main {
  val appEnv = Environment simple (mode = Mode.Dev)
  val appLoadingCtx = ApplicationLoader createContext appEnv

  def main(args: Array[String]): Unit = {
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val m = create(); import m._

    try {
      val repos -> elapsed = timed(getFullOrgRepos(org).await30s)
      repos pipe (rs => s"${rs.length} repos".>>)
      s"Took: ${elapsed.toHHmmssSSS}".>>
    } catch {
      case e @ JsResultException(errors) =>
        s"JSON errors:\n${JsError(errors).toJson.pp}".>>
        throw e
    } finally stop()
  }

  def create() = {
    val accessToken =
      sys.env get "GITHUB_API_TOKEN" map AccessToken getOrElse (sys error "Need to set GITHUB_API_TOKEN")

    val connectionConfig = ConnectionConfig create accessToken

    new Main(connectionConfig)
  }
}

class Main(connectionConfig: ConnectionConfig)
  extends BuiltInComponentsFromContext(Main.appLoadingCtx)
     with play.api.libs.ws.ning.NingWSComponents
{

  val router = routing.Router.empty
  val gh = new GitHubApi(wsClient, connectionConfig, actorSystem)
  import actorSystem.dispatcher

  def getFullOrgRepos(org: String) =
    gh.repos getOrgRepos org flatMap {
      _ traverse { r =>
        gh.repos.getRepo(r.owner.login, r.name)
      }
    }

  def getReposWithNonPrimaryLanguage(org: String, lang: String) = {
    val zrs = gh.repos.getOrgRepos(org)
    val rals = zrs flatMap (_ traverse (r => gh.repos.getRepoLanguage(r.owner.login, r.name) map r.->))
    val nonLang = rals map (_ filterNot (_._1.language contains lang))
    val poorLang = nonLang map (_ filter (_._2 contains lang))
    poorLang map (_ map (r => r._1.name +: (r._1.language getOrElse "-") +: r._2.toVector.sortBy(x => -x._2)) showM)
  }

  def stop(): Unit =
    try
      applicationLifecycle.stop() await Duration.Inf
    catch {
      case NonFatal(e) => Logger(Main.getClass).warn("Error stopping.", e)
    }
}
