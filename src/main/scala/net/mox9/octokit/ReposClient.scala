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
  implicit val jsonFormat: OFormat[User] = Json.format[User]
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

  permissions       : Option[RepoPermissions]
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
    (__ \ "language"          ) .readNullable[String]          and
    (__ \ "forks_count"       ) .read[Int]                     and
    (__ \ "stargazers_count"  ) .read[Int]                     and
    (__ \ "watchers_count"    ) .read[Int]                     and
    (__ \ "size"              ) .read[Int]                     and
    (__ \ "default_branch"    ) .read[String]                  and
    (__ \ "open_issues_count" ) .read[Int]                     and
    (__ \ "has_issues"        ) .read[Boolean]                 and
    (__ \ "has_wiki"          ) .read[Boolean]                 and
    (__ \ "has_pages"         ) .read[Boolean]                 and
    (__ \ "has_downloads"     ) .read[Boolean]                 and
    (__ \ "pushed_at"         ) .read[ZonedDateTime]           and
    (__ \ "created_at"        ) .read[ZonedDateTime]           and
    (__ \ "updated_at"        ) .read[ZonedDateTime]
  ).tupled

  val reads4 = (__ \ "permissions").readNullable[RepoPermissions]

  val jsonReads: Reads[RepoSummary] =
    reads1 and reads2 and reads3 and reads4 apply { (v1, v2, v3, permissions) =>
      val (id, owner, name, full_name, description, private1, fork) = v1
      val (url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage) = v2
      val (
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at
      ) = v3
      RepoSummary(
        id, owner, name, full_name, description, private1, fork,
        url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage,
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at, permissions
      )
    }

  val writes1 =
    OWrites[RepoSummary] { rs => import rs._
      Json.obj(
        "id"                -> id,
        "owner"             -> owner,
        "name"              -> name,
        "full_name"         -> full_name,
        "description"       -> description,
        "private"           -> `private`,
        "fork"              -> fork,
        "url"               -> url,
        "html_url"          -> html_url,
        "clone_url"         -> clone_url,
        "git_url"           -> git_url,
        "ssh_url"           -> ssh_url,
        "svn_url"           -> svn_url,
        "mirror_url"        -> mirror_url,
        "homepage"          -> homepage,
        "language"          -> language,
        "forks_count"       -> forks_count,
        "stargazers_count"  -> stargazers_count,
        "watchers_count"    -> watchers_count,
        "size"              -> size,
        "default_branch"    -> default_branch,
        "open_issues_count" -> open_issues_count,
        "has_issues"        -> has_issues,
        "has_wiki"          -> has_wiki,
        "has_pages"         -> has_pages,
        "has_downloads"     -> has_downloads,
        "pushed_at"         -> pushed_at,
        "created_at"        -> created_at,
        "updated_at"        -> updated_at
      )
    }

  val writes2 = OWrites[RepoSummary](rs => Json.obj("permissions" -> rs.permissions))

  val jsonWrites: OWrites[RepoSummary] = (writes1 ~ writes2).join

  implicit val jsonFormat: OFormat[RepoSummary] = OFormat(jsonReads, jsonWrites)
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
  organization      : Option[User],
  parent            : Option[RepoSummary],
  source            : Option[RepoSummary]
)
object Repo {
  import RepoSummary._
  val reads4 = (
    (__ \ "permissions"       ) .read[RepoPermissions]     and
    (__ \ "subscribers_count" ) .read[Int]                 and
    (__ \ "organization"      ) .readNullable[User]        and
    (__ \ "parent"            ) .readNullable[RepoSummary] and
    (__ \ "source"            ) .readNullable[RepoSummary]
  ).tupled

  val jsonReads: Reads[Repo] =
    reads1 and reads2 and reads3 and reads4 apply { (v1, v2, v3, v4) =>
      val (id, owner, name, full_name, description, private1, fork) = v1
      val (url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage) = v2
      val (
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at
      ) = v3
      val (permissions, subscribers_count, organization, parent, source) = v4
      Repo(
        id, owner, name, full_name, description, private1, fork,
        url, html_url, clone_url, git_url, ssh_url, svn_url, mirror_url, homepage,
        language, fork_count, stargazers_count, watchers_count, size, default_branch, open_issues_count,
        has_issues, has_wiki, has_pages, has_downloads, pushed_at, created_at, updated_at, permissions,
        subscribers_count, organization, parent, source
      )
    }

  val writes2 =
    OWrites[Repo] { rs => import rs._
      Json.obj(
        "permissions"       -> permissions,
        "subscribers_count" -> subscribers_count,
        "organization"      -> organization,
        "parent"            -> parent,
        "source"            -> source
      )
    }

  val jsonWrites: OWrites[Repo] =
    (writes1 ~ writes2) { r =>
      val rs = RepoSummary(
        id                = r.id,
        owner             = r.owner,
        name              = r.name,
        full_name         = r.full_name,
        description       = r.description,
        `private`         = r.`private`,
        fork              = r.fork,

        url               = r.url,
        html_url          = r.html_url,
        clone_url         = r.clone_url,
        git_url           = r.git_url,
        ssh_url           = r.ssh_url,
        svn_url           = r.svn_url,
        mirror_url        = r.mirror_url,
        homepage          = r.homepage,

        language          = r.language,
        forks_count       = r.forks_count,
        stargazers_count  = r.stargazers_count,
        watchers_count    = r.watchers_count,
        size              = r.size,
        default_branch    = r.default_branch,
        open_issues_count = r.open_issues_count,
        has_issues        = r.has_issues,
        has_wiki          = r.has_wiki,
        has_pages         = r.has_pages,
        has_downloads     = r.has_downloads,
        pushed_at         = r.pushed_at,
        created_at        = r.created_at,
        updated_at        = r.updated_at,

        permissions       = r.permissions.some
      )
      (rs, r)
    }

  implicit val jsonFormat: OFormat[Repo] = OFormat(jsonReads, jsonWrites)
}

final case class Contributor(
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
  site_admin          : Boolean,
  contributions       : Int
)
object Contributor {
  val reads2  = (__ \ "contributions").read[Int]
  val writes2 = OWrites[Contributor](c => Json.obj("contributions" -> c.contributions))

  val jsonReads: Reads[Contributor] =
    (Reads.of[User] ~ reads2) { (user, contributions) =>
      Contributor(
        login               = user.login,
        id                  = user.id,
        avatar_url          = user.avatar_url,
        gravatar_id         = user.gravatar_id,
        url                 = user.url,
        html_url            = user.html_url,
        followers_url       = user.followers_url,
        following_url       = user.following_url,
        gists_url           = user.gists_url,
        starred_url         = user.starred_url,
        subscriptions_url   = user.subscriptions_url,
        organizations_url   = user.organizations_url,
        repos_url           = user.repos_url,
        events_url          = user.events_url,
        received_events_url = user.received_events_url,
        `type`              = user.`type`,
        site_admin          = user.site_admin,
        contributions       = contributions
      )
    }

  val jsonWrites: OWrites[Contributor] =
    (implicitly[OWrites[User]] ~ writes2).apply { c =>
      val u = User(
        login               = c.login,
        id                  = c.id,
        avatar_url          = c.avatar_url,
        gravatar_id         = c.gravatar_id,
        url                 = c.url,
        html_url            = c.html_url,
        followers_url       = c.followers_url,
        following_url       = c.following_url,
        gists_url           = c.gists_url,
        starred_url         = c.starred_url,
        subscriptions_url   = c.subscriptions_url,
        organizations_url   = c.organizations_url,
        repos_url           = c.repos_url,
        events_url          = c.events_url,
        received_events_url = c.received_events_url,
        `type`              = c.`type`,
        site_admin          = c.site_admin
      )
      (u, c)
    }

  implicit val jsonFormat: OFormat[Contributor] = OFormat[Contributor](jsonReads, jsonWrites)
}

/** @see https://developer.github.com/v3/repos/ */
final class ReposClient(gh: GitHubClient, actorSystem: ActorSystem) {
  import actorSystem.dispatcher

  def getYourRepos()                 : Future[Seq[RepoSummary]] = getReposAtUrl(s"/user/repos")
  def getUserRepos(username: String) : Future[Seq[RepoSummary]] = getReposAtUrl(s"/users/$username/repos")
  def getOrgRepos(org: String)       : Future[Seq[RepoSummary]] = getReposAtUrl(s"/orgs/$org/repos")

  def getRepo(owner: String, repo: String): Future[Repo] =
    gh url s"/repos/$owner/$repo" get() map (_.json.as[Repo])

  def getRepoContributors(owner: String, repo: String): Future[Seq[Contributor]] = (
    gh
      url s"/repos/$owner/$repo/contributors"
      get()
      flatMap {
        case r if r.status == 200 => r.json.as[Seq[Contributor]].future
        case r if r.status == 204 => Nil.future
        case r if r.status == 403 => handle403(r)
        case r                    =>
          sys error s"Unhandled status: ${r.status}, body:\n${r.body}"
      }
  )

  def getRepoLanguage(owner: String, repo: String): Future[Map[String, Int]] =
    gh url s"/repos/$owner/$repo/languages" get() map (_.json.as[Map[String, Int]])

  private def getReposAtUrl(path: String): Future[Seq[RepoSummary]] =
    (getReposResp(path, 1)
      flatMap {
        case r if r.status == 200 =>
          r.json.as[Seq[RepoSummary]] match {
            case jsError: JsError => jsError.future
            case reposJson        =>
              val remainingReposJson = r header "Link" flatMap getPageCount match {
                case Some(pageCount) => (2 to pageCount).toVector traverse (getReposJson(path, _))
                case None            => Vector.empty.future
              }
              remainingReposJson.foldMap(reposJson)(_ |+| _)
          }
        case r if r.status == 204 => Nil.future
        case r if r.status == 403 => handle403(r)
        case r                    => sys error s"Unhandled status: ${r.status}, body:\n${r.body}"
      }
    )

  private def getReposJson(path: String, pageNum: Int): Future[Seq[RepoSummary]] =
    getReposResp(path, pageNum) map (_.json.as[Seq[RepoSummary]])

  private def getReposResp(path: String, pageNum: Int): Future[WSResponse] =
    (gh
      url path
      withQueryString "page" -> s"$pageNum"
      withQueryString "sort" -> "updated"
      get()
    )

  private def getPageCount(link: String) =
    """<.+[?&]page=(\d+).*>; rel="last"""".r findFirstMatchIn link map (_ group 1 toInt)

  private def handle403(r: WSResponse) = {
    val reset1 = r header "X-RateLimit-Reset" map (_.toLong) map Instant.ofEpochSecond
    val reset2 = reset1 map (i => s"(reset at $i)") getOrElse ""
    sys error s"Probably rate limited$reset2, got: ${r.status}, body: \n${r.json.pp}"
    // X-RateLimit-Limit        The maximum number of requests that the consumer is permitted to make per hour.
    // X-RateLimit-Remaining    The number of requests remaining in the current rate limit window.
    // X-RateLimit-Reset        The time at which the current rate limit window resets in UTC epoch seconds.
  }
}
