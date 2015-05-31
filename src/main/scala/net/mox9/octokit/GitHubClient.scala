package net.mox9.octokit

final case class UserAgent(value: String) extends AnyVal with StringVal

sealed trait Credentials extends Any
final case class BasicAuth(user: String, pass: String) extends Credentials
final case class AccessToken(value: String) extends AnyVal with StringVal with Credentials

// TODO: Use Credentials
final case class ConnectionConfig(userAgent: UserAgent, accessToken: AccessToken)

final class GitHubClient(ws: WSClient, connectionConfig: ConnectionConfig) {
  val orgs = new OrgsClient(ws, connectionConfig)
}

final case class Repo(name: String, `private`: Boolean, fork: Boolean, language: Option[String])
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}
