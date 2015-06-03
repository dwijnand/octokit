package net.mox9.octokit

final case class UserAgent(value: String) extends AnyVal with StringVal
object UserAgent extends (String => UserAgent) {
  implicit def lift(s: String) = UserAgent(s)
}

final case class AccessToken(value: String) extends AnyVal with StringVal
object AccessToken extends (String => AccessToken) {
  implicit def lift(s: String) = AccessToken(s)
}

final case class ConnectionConfig(accessToken: AccessToken, userAgent: UserAgent)
object ConnectionConfig {
  def create(accessToken: AccessToken) = ConnectionConfig(accessToken, "Octokit Scala Client")
}

final class GitHubApi(ws: WSClient, connectionConfig: ConnectionConfig, actorSystem: ActorSystem) {
  val gh    = new GitHubClient(ws: WSClient, connectionConfig)
  val repos = new ReposClient(gh, actorSystem)
}

final class GitHubClient(val ws: WSClient, val connectionConfig: ConnectionConfig) {
  // TODO: Rename to path, and type it perhaps
  def url(path: String): WSRequest = {
    (ws
      url s"https://api.github.com$path"
      withHeaders    "User-Agent" -> s"${connectionConfig.userAgent}"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token ${connectionConfig.accessToken}"
    )
  }
}
