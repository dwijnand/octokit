package net.mox9.skithub

import play.api.Play.current
import play.api.libs.json.JsResultException
//import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS
import play.api.{ DefaultApplication, Mode, Play }

import scala.concurrent.ExecutionContext.Implicits._
import java.io.File

final case class UserAgent(value: String) extends AnyVal {
  override def toString = value
}

sealed trait Credentials extends Any
final case class BasicAuth(user: String, pass: String) extends Credentials
final case class AccessToken(value: String) extends AnyVal with Credentials {
  override def toString = value
}

// TODO: Use Credentials
case class ConnectionConfig(userAgent: UserAgent, accessToken: AccessToken)

final class GitHubClient(connectionConfig: ConnectionConfig) {
  val orgs = new OrgsClient(connectionConfig)
}

final class OrgsClient(connectionConfig: ConnectionConfig) {
  def getRepos(org: String): Future[Seq[Repo]] = (
    getRepos1(connectionConfig, s"https://api.github.com/orgs/$org/repos", Vector.empty)
      flatMap {
        case JsSuccess(repos, _) => Future successful repos
        case JsError(errors)     => Future failed JsResultException(errors)
      }
  )

  private def getRepos1(
    connectionConfig: ConnectionConfig, urlStr: String, repos: Vector[Repo]
  ): Future[JsResult[Seq[Repo]]] =
    (WS
      url urlStr
      withHeaders    "User-Agent" -> s"${connectionConfig.userAgent}"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token ${connectionConfig.accessToken}"
      get()
      flatMap { resp =>
        resp.json.validate[Seq[Repo]] match {
          case JsSuccess(moreRepos, _) =>
            resp header "Link" flatMap getNextLink match {
              case Some(nextUrlStr) => getRepos1(connectionConfig, nextUrlStr, repos ++ moreRepos)
              case None             => Future successful JsSuccess(repos ++ moreRepos)
            }
          case jsE: JsError            => Future successful jsE
        }
      }
    )

  private def getNextLink(link: String) = """<(.+)>; rel="next"""".r findFirstMatchIn link map (_ group 1)
}

object T {
  val userAgent = UserAgent("dwijnand")

  val accessToken =
    sys.env get "GITHUB_API_TOKEN"  map AccessToken getOrElse (sys error "Need to set GITHUB_API_TOKEN")

  val connectionConfig = ConnectionConfig(userAgent, accessToken)

  val github = new GitHubClient(connectionConfig)

  def main(args: Array[String]): Unit = {
    val org = args.headOption getOrElse (sys error "Provide an org name")

    start()

    try {
      val repos = github.orgs.getRepos(org)
      repos.await30s pipe (rs => s"${rs.length} repos".>>)
    }
    finally Play.stop()
  }

  def newApp = new DefaultApplication(new File("."), this.getClass.getClassLoader, None, Mode.Dev)

  def start() = Play start newApp
  def stop()  = Play.stop()
}

// ISO-8601: YYYY-MM-DDTHH:MM:SSZ

final case class Repo(
  name      : String,
  `private` : Boolean,
  fork      : Boolean,
  language  : Option[String]
)
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}
