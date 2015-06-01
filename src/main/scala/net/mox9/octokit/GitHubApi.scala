package net.mox9.octokit

final case class UserAgent(value: String) extends AnyVal with StringVal
final case class AccessToken(value: String) extends AnyVal with StringVal
final case class ConnectionConfig(userAgent: UserAgent, accessToken: AccessToken)

final class GitHubApi(ws: WSClient, connectionConfig: ConnectionConfig, actorSystem: ActorSystem) {
  val gh    = new GitHubClient(ws: WSClient, connectionConfig)
  val repos = new ReposClient(gh, actorSystem)
}

final class GitHubClient(val ws: WSClient, val connectionConfig: ConnectionConfig) {
  def url(urlStr: String): WSRequest = {
    (ws
      url urlStr
      withHeaders    "User-Agent" -> s"${connectionConfig.userAgent}"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token ${connectionConfig.accessToken}"
    )
  }
}

final case class Repo(name: String, `private`: Boolean, fork: Boolean, language: Option[String])
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}
