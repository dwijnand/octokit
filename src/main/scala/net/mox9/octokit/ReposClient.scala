package net.mox9.octokit

import play.api.libs.functional.syntax._

final case class Repo(name: String, `private`: Boolean, fork: Boolean, language: Option[String])
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}

// Alternative listYourRepos / listUserRepos
/** @see https://developer.github.com/v3/repos/ */
final class ReposClient(gh: GitHubClient, actorSystem: ActorSystem) {
  import actorSystem.dispatcher

  def getRepos()               : Future[Seq[Repo]] = getReposAtUrl(s"https://api.github.com/user/repos")
  def getOrgRepos(org: String) : Future[Seq[Repo]] = getReposAtUrl(s"https://api.github.com/orgs/$org/repos")

  private def getReposAtUrl(urlStr: String): Future[Seq[Repo]] =
    (getReposResp(urlStr, 1)
      flatMap { resp =>
        resp.json.validate[Seq[Repo]] match {
          case jsError: JsError => jsError.future
          case reposJson        =>
            val remainingReposJson = resp header "Link" flatMap getPageCount match {
              case Some(pageCount) => (2 to pageCount).toVector traverse (getReposJson(urlStr, _))
              case None            => Vector.empty.future
            }
            remainingReposJson.foldMap(reposJson)(_ |+| _)
        }
      }
      flatten
    )

  private def getReposJson(urlStr: String, pageNum: Int): Future[JsResult[Seq[Repo]]] =
    getReposResp(urlStr, pageNum) map (_.json.validate[Seq[Repo]])

  private def getReposResp(urlStr: String, pageNum: Int): Future[WSResponse] =
    (gh
      url urlStr
      withQueryString "page" -> s"$pageNum"
      withQueryString "sort" -> "updated"
      get()
    )

  private def getPageCount(link: String) =
    """<.+[?&]page=(\d+).*>; rel="last"""".r findFirstMatchIn link map (_ group 1 toInt)
}
