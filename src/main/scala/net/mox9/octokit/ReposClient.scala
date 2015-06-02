package net.mox9.octokit

import play.api.libs.functional.syntax._

import java.time._

final case class User(
  login               : String,
  id                  : Int,
  avatar_url          : Url,
  gravatar_id         : String,
  url                 : Url,
  html_url            : Url,
  followers_url       : Url,
  following_url       : Url,
  gists_url           : Url,
  starred_url         : Url,
  subscriptions_url   : Url,
  organizations_url   : Url,
  repos_url           : Url,
  events_url          : Url,
  received_events_url : Url,
  `type`              : String,
  site_admin          : Boolean
)
object User {
  implicit val jsonFormat: JsonFormat[User] = Json.format[User]
}

final case class RepoPermissions(admin: Boolean, push: Boolean, pull: Boolean)
object RepoPermissions {
  implicit val jsonFormat: JsonFormat[RepoPermissions] = Json.format[RepoPermissions]
}

final case class RepoSummary(
  id                : Long,
  owner             : User,
  name              : String,
  full_name         : String,
  description       : Option[String],
  `private`         : Boolean,
  fork              : Boolean,

  url               : Url,
  html_url          : Url,
  clone_url         : Url,
  git_url           : Url,
  ssh_url           : Url,
  svn_url           : Url,
  mirror_url        : Option[Url],
  homepage          : Option[Url],

  language          : Option[String],
  forks_count       : Int,
  stargazers_count  : Int,
  watchers_count    : Int,
  size              : Int,
  default_branch    : String,
  open_issues_count : Int,
  has_issues        : Boolean,
  has_wiki          : Boolean,
  has_pages         : Boolean,
  has_downloads     : Boolean,
  pushed_at         : ZonedDateTime,
  created_at        : ZonedDateTime,
  updated_at        : ZonedDateTime,
  permissions       : RepoPermissions
)
object RepoSummary {
  val reads1 = (
    (__ \ "id"          ) .read[Long]           and
    (__ \ "owner"       ) .read[User]           and
    (__ \ "name"        ) .read[String]         and
    (__ \ "full_name"   ) .read[String]         and
    (__ \ "description" ) .readNullable[String] and
    (__ \ "private"     ) .read[Boolean]        and
    (__ \ "fork"        ) .read[Boolean]
  ).tupled

  val reads2 = (
    (__ \ "url"        ) .read[Url] and
    (__ \ "html_url"   ) .read[Url] and
    (__ \ "clone_url"  ) .read[Url] and
    (__ \ "git_url"    ) .read[Url] and
    (__ \ "ssh_url"    ) .read[Url] and
    (__ \ "svn_url"    ) .read[Url] and
    (__ \ "mirror_url" ) .readNullable[Url] and
    (__ \ "homepage"   ) .readNullable[Url]
  ).tupled

  val reads3 = (
    (__ \ "language"          ) .readNullable[String]  and
    (__ \ "forks_count"       ) .read[Int]             and
    (__ \ "stargazers_count"  ) .read[Int]             and
    (__ \ "watchers_count"    ) .read[Int]             and
    (__ \ "size"              ) .read[Int]             and
    (__ \ "default_branch"    ) .read[String]          and
    (__ \ "open_issues_count" ) .read[Int]             and
    (__ \ "has_issues"        ) .read[Boolean]         and
    (__ \ "has_wiki"          ) .read[Boolean]         and
    (__ \ "has_pages"         ) .read[Boolean]         and
    (__ \ "has_downloads"     ) .read[Boolean]         and
    (__ \ "pushed_at"         ) .read[ZonedDateTime]   and
    (__ \ "created_at"        ) .read[ZonedDateTime]   and
    (__ \ "updated_at"        ) .read[ZonedDateTime]   and
    (__ \ "permissions"       ) .read[RepoPermissions]
  ).tupled

  implicit val jsonReads: Reads[RepoSummary] =
    reads1 and reads2 and reads3 apply { (v1, v2, v3) =>
      val (id, owner, name, full_name, description, private1, fork) = v1
      val (url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage) = v2
      val (
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at, permissions
      ) = v3
      RepoSummary(
        id, owner, name, full_name, description, private1, fork,
        url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage,
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at, permissions
      )
    }
}

final case class Repo(
  id                : Long,
  owner             : User,
  name              : String,
  full_name         : String,
  description       : Option[String],
  `private`         : Boolean,
  fork              : Boolean,

  url               : Url,
  html_url          : Url,
  clone_url         : Url,
  git_url           : Url,
  ssh_url           : Url,
  svn_url           : Url,
  mirror_url        : Option[Url],
  homepage          : Option[Url],

  language          : Option[String],
  forks_count       : Int,
  stargazers_count  : Int,
  watchers_count    : Int,
  size              : Int,
  default_branch    : String,
  open_issues_count : Int,
  has_issues        : Boolean,
  has_wiki          : Boolean,
  has_pages         : Boolean,
  has_downloads     : Boolean,
  pushed_at         : ZonedDateTime,
  created_at        : ZonedDateTime,
  updated_at        : ZonedDateTime,
  permissions       : RepoPermissions,

  subscribers_count : Int,
  organization      : User,
  parent            : RepoSummary,
  source            : RepoSummary
)
object Repo {
  import RepoSummary._
  val reads4 = (
    (__ \ "subscribers_count" ) .read[Int]         and
    (__ \ "organization"      ) .read[User]        and
    (__ \ "parent"            ) .read[RepoSummary] and
    (__ \ "source"            ) .read[RepoSummary]
  ).tupled

  implicit val reads: Reads[Repo] =
    reads1 and reads2 and reads3 and reads4 apply { (v1, v2, v3, v4) =>
      val (id, owner, name, full_name, description, private1, fork) = v1
      val (url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage) = v2
      val (
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at, permissions
      ) = v3
      val (subscribers_count, organization, parent, source) = v4
      Repo(
        id, owner, name, full_name, description, private1, fork,
        url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage,
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at, permissions,
        subscribers_count, organization, parent, source
      )
    }
}

// Alternative listYourRepos / listUserRepos
/** @see https://developer.github.com/v3/repos/ */
final class ReposClient(gh: GitHubClient, actorSystem: ActorSystem) {
  import actorSystem.dispatcher

  def getRepos()               : Future[Seq[RepoSummary]] = getReposAtUrl(s"/user/repos")
  def getOrgRepos(org: String) : Future[Seq[RepoSummary]] = getReposAtUrl(s"/orgs/$org/repos")

  def getRepo(owner: String, repo: String): Future[Repo] =
    gh url s"/repos/$owner/$repo" get() map (_.json.validate[Repo]) flatten

  private def getReposAtUrl(path: String): Future[Seq[RepoSummary]] =
    (getReposResp(path, 1)
      flatMap { resp =>
        resp.json.validate[Seq[RepoSummary]] match {
          case jsError: JsError => jsError.future
          case reposJson        =>
            val remainingReposJson = resp header "Link" flatMap getPageCount match {
              case Some(pageCount) => (2 to pageCount).toVector traverse (getReposJson(path, _))
              case None            => Vector.empty.future
            }
            remainingReposJson.foldMap(reposJson)(_ |+| _)
        }
      }
      flatten
    )

  private def getReposJson(path: String, pageNum: Int): Future[JsResult[Seq[RepoSummary]]] =
    getReposResp(path, pageNum) map (_.json.validate[Seq[RepoSummary]])

  private def getReposResp(path: String, pageNum: Int): Future[WSResponse] =
    (gh
      url path
      withQueryString "page" -> s"$pageNum"
      withQueryString "sort" -> "updated"
      get()
    )

  private def getPageCount(link: String) =
    """<.+[?&]page=(\d+).*>; rel="last"""".r findFirstMatchIn link map (_ group 1 toInt)
}
