package net.mox9.skithub

import play.api.Play.current
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
final class GitHubClient(userAgent: UserAgent, accessToken: AccessToken)

object T {
  def main(args: Array[String]): Unit = {
    val userAgent = "dwijnand"
    val accessToken = sys.env get "GITHUB_API_TOKEN"  map AccessToken getOrElse
      (sys error "Need to set GITHUB_API_TOKEN")
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val appBaseDir = new File(".")
    val appClassLoader = this.getClass.getClassLoader

    Play start new DefaultApplication(appBaseDir, appClassLoader, None, Mode.Dev)

    try go(userAgent, accessToken, org)
    finally Play.stop()
  }

  def go(userAgent: String, accessToken: AccessToken, org: String): Unit = {
    val urlStr = s"https://api.github.com/orgs/$org/repos"

    val repos: Seq[Repo] = getRepos(userAgent, accessToken, urlStr).await30s

    s"${repos.length} repos".>>
//    repos foreach (_.>>)

    ()
  }

  def getRepos(userAgent: String, accessToken: AccessToken, urlStr: String): Future[Seq[Repo]] = (
    getRepos1(userAgent, accessToken, urlStr, Vector.empty)
      map (_ recoverTotal (e => sys error s"Failed to read repos:\n${e.toFlatJson.pp}"))
  )

  def getRepos1(
    userAgent: String, accessToken: AccessToken, urlStr: String, repos: Vector[Repo]
  ): Future[JsResult[Seq[Repo]]] =
    (WS
      url urlStr
      withHeaders    "User-Agent" -> userAgent
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token $accessToken"
      get()
      flatMap { resp =>
        resp.json.validate[Seq[Repo]] match {
          case JsSuccess(moreRepos, _) =>
            resp header "Link" flatMap getNextLink match {
              case Some(nextUrlStr) => getRepos1(userAgent, accessToken, nextUrlStr, repos ++ moreRepos)
              case None             => Future successful JsSuccess(repos ++ moreRepos)
            }
          case jsE: JsError            => Future successful jsE
        }
      }
    )

  def getNextLink(link: String) = """<(.+)>; rel="next"""".r findFirstMatchIn link map (_ group 1)
}

// ISO-8601: YYYY-MM-DDTHH:MM:SSZ

final case class Repo(
  name      : String,
  full_name : String,
  `private` : Boolean,
  fork      : Boolean,
  language  : Option[String]
)
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}
