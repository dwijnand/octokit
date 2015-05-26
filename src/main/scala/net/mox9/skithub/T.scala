package net.mox9.skithub

import play.api.Play.current
import play.api.libs.json.JsResultException
//import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{ WS, WSResponse }
import play.api.{ DefaultApplication, Mode, Play }

import scala.concurrent.ExecutionContext.Implicits._
import java.io.File

case class UserAgent(value: String) extends AnyVal {
  override def toString = value
}

sealed trait Credentials extends Any
final case class BasicAuth(user: String, pass: String) extends Credentials
final case class AccessToken(value: String) extends AnyVal with Credentials {
  override def toString = value
}

// TODO: Use Credentials
final case class ConnectionConfig(userAgent: UserAgent, accessToken: AccessToken)

final class GitHubClient(connectionConfig: ConnectionConfig) {
  val orgs = new OrgsClient(connectionConfig)
}

final class OrgsClient(connectionConfig: ConnectionConfig) {
  def getRepos(org: String): Future[Seq[Repo]] =
    (getRepos1(org, 1)
      flatMap { resp =>
        resp.json.validate[Seq[Repo]] match {
          case JsSuccess(repos, _) =>
            resp header "Link" flatMap getPageCount match {
              case Some(pageCount) =>
                (2 to pageCount
                  map (p => getRepos1(org, p) map (_.json.validate[Seq[Repo]]))
                  futSeq()
                  map (_ reduce ((res1, res2) => for (rs1 <- res1; rs2 <- res2) yield rs1 ++ rs2))
                )
              case None            => Future successful JsSuccess(repos)
            }
          case jsError             => Future successful jsError
        }
      }
      flatMap {
        case JsSuccess(repos, _) => Future successful repos
        case JsError(errors)     => Future failed JsResultException(errors)
      }
    )

  private def getRepos1(org: String, pageNum: Int): Future[WSResponse] =
    (WS
      url s"https://api.github.com/orgs/$org/repos"
      withQueryString      "page" -> s"$pageNum"
      withHeaders    "User-Agent" -> s"${connectionConfig.userAgent}"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token ${connectionConfig.accessToken}"
      get()
    )

  private def getPageCount(link: String) =
    """<.+[?&]page=(\d+).*>; rel="last"""".r findFirstMatchIn link map (_ group 1 toInt)
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
      val repos -> elapsed = timed {
        github.orgs getRepos org await30s
      }
      repos pipe (rs => s"${rs.length} repos".>>)

      s"Took: ${elapsed.toHHmmssSSS}".>>
    } finally stop()
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
