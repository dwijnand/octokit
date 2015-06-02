package net.mox9.octokit

import play.api._

object Main {
  val userAgent = UserAgent("dwijnand")

  // TODO: Move this out of T object
  val accessToken =
    sys.env get "GITHUB_API_TOKEN" map AccessToken getOrElse (sys error "Need to set GITHUB_API_TOKEN")

  val appEnv = Environment simple (mode = Mode.Dev)
  val appLoadingCtx = ApplicationLoader createContext appEnv

  val connectionConfig = ConnectionConfig(userAgent, accessToken)

  def main(args: Array[String]): Unit = {
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val m = new Main; import m._

    try {
      val repos -> elapsed = timed {
        github.repos getOrgRepos org await30s
      }
      repos pipe (rs => s"${rs.length} repos".>>)

      s"Took: ${elapsed.toHHmmssSSS}".>>
    } catch {
      case JsResultException(errors) => s"JSON errors:\n${JsError(errors).toJson.pp}".>>
    } finally stop()
  }
}

class Main
  extends BuiltInComponentsFromContext(Main.appLoadingCtx)
     with play.api.libs.ws.ning.NingWSComponents
{
  import Main._

  val router = routing.Router.empty
  val github = new GitHubApi(wsClient, connectionConfig, actorSystem)

  def stop(): Unit =
    try
      applicationLifecycle.stop() await Duration.Inf
    catch {
      case NonFatal(e) => Logger(Main.getClass).warn("Error stopping.", e)
    }
}
