package net.mox9.octokit

import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext.Implicits._

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
