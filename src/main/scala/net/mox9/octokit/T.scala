package net.mox9.octokit

import play.api._

object T {
  val userAgent = UserAgent("dwijnand")

  val accessToken =
    sys.env get "GITHUB_API_TOKEN" map AccessToken getOrElse (sys error "Need to set GITHUB_API_TOKEN")

  val appEnv = Environment simple (mode = Mode.Dev)
  val appLoadingCtx = ApplicationLoader createContext appEnv

  val connectionConfig = ConnectionConfig(userAgent, accessToken)

  def main(args: Array[String]): Unit = {
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val t = new T; import t._

    try {
      val repos -> elapsed = timed {
        github.orgs getRepos org await30s
      }
      repos pipe (rs => s"${rs.length} repos".>>)

      s"Took: ${elapsed.toHHmmssSSS}".>>
    } finally stop()
  }
}

class T
  extends BuiltInComponentsFromContext(T.appLoadingCtx)
     with play.api.libs.ws.ning.NingWSComponents
{
  import T._

  val router = routing.Router.empty
  val github = new GitHubClient(wsClient, connectionConfig)

  def stop(): Unit =
    try
      applicationLifecycle.stop() await Duration.Inf
    catch {
      case NonFatal(e) => Logger(T.getClass).warn("Error stopping.", e)
    }
}
