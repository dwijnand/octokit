package net.mox9.octokit

//import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext.Implicits._

case class UserAgent(value: String) extends AnyVal {
  override def toString = value
}

sealed trait Credentials extends Any
final case class BasicAuth(user: String, pass: String) extends Credentials
case class AccessToken(value: String) extends AnyVal with Credentials {
  override def toString = value
}

// TODO: Use Credentials
final case class ConnectionConfig(userAgent: UserAgent, accessToken: AccessToken)

final class GitHubClient(ws: WSClient, connectionConfig: ConnectionConfig) {
  val orgs = new OrgsClient(ws, connectionConfig)
}

final class OrgsClient(ws: WSClient, connectionConfig: ConnectionConfig) {
  /** @see https://developer.github.com/v3/repos/#list-organization-repositories */
  def getRepos(org: String): Future[Seq[Repo]] =
    (getReposResp(org, 1)
      flatMap { resp =>
        resp.json.validate[Seq[Repo]] match {
          case jsError: JsError => jsError.future
          case reposJson        =>
            val remainingReposJson = resp header "Link" flatMap getPageCount match {
              case Some(pageCount) => (2 to pageCount).toVector traverse (getReposJson(org, _))
              case None            => Vector.empty.future
            }
            remainingReposJson.foldMap(reposJson)(_ |+| _)
        }
      }
      flatten
    )

  private def getReposJson(org: String, pageNum: Int): Future[JsResult[Seq[Repo]]] =
    getReposResp(org, pageNum) map (_.json.validate[Seq[Repo]])

  private def getReposResp(org: String, pageNum: Int): Future[WSResponse] =
    (ws
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

// ISO-8601: YYYY-MM-DDTHH:MM:SSZ

// TODO: Consider Private/PublicRepo, Fork/Mirror/SourceRepo, & isPrivate/isFork/etc ops.
final case class Repo(
  name      : String,
  `private` : Boolean,
  fork      : Boolean,
  language  : Lang
)
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}

// TODO: Revert this back to Option[String], sort pprinting elsewhere & output None as "-"
// TODO: Alternatively see if this can be factored out to reduce noise/boilerplate
sealed trait Lang extends Any { def value: String ; final override def toString = value }

object Lang extends (String => Lang) {
  def apply(s: String): Lang = s.trim pipe (s => if (s.isEmpty) NoLang else LangImpl(s))

  implicit val jsFormat: JsonFormat[Lang] =
    JsonFormat(
      Reads.optionNoError[String] map { case None => NoLang ; case Some(s) => Lang(s) },
      Writes(_.value.toJson)
    )
}

case object NoLang extends Lang { val value = "" }

case class LangImpl(value: String) extends AnyVal with Lang
