package net.mox9.octokit

import play.api.libs.functional.syntax._

final class ReposClient(ws: WSClient, connectionConfig: ConnectionConfig, actorSystem: ActorSystem) {
  import actorSystem.dispatcher

  /** @see https://developer.github.com/v3/repos/#list-organization-repositories */
  def getOrgRepos(org: String): Future[Seq[Repo]] = getReposAtUrl(s"https://api.github.com/orgs/$org/repos")

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
    (ws
      url urlStr
      withQueryString      "page" -> s"$pageNum"
      withHeaders    "User-Agent" -> s"${connectionConfig.userAgent}"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token ${connectionConfig.accessToken}"
      get()
    )

  private def getPageCount(link: String) =
    """<.+[?&]page=(\d+).*>; rel="last"""".r findFirstMatchIn link map (_ group 1 toInt)
}
