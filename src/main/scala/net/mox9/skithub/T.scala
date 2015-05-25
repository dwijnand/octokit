package net.mox9.skithub

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS
import play.api.{ DefaultApplication, Mode, Play }

import java.io.File

object T {
  def main(args: Array[String]): Unit = {
    val accessToken = sys.env get "GITHUB_API_TOKEN"  map AccessToken getOrElse
      (sys error "Need to set GITHUB_API_TOKEN")
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val appBaseDir = new File(".")
    val appClassLoader = this.getClass.getClassLoader

    Play start new DefaultApplication(appBaseDir, appClassLoader, None, Mode.Dev)

    try go(accessToken, org)
    finally Play.stop()
  }

  def go(accessToken: AccessToken, org: String): Unit = {
    val urlStr = s"https://api.github.com/orgs/$org/repos"

    val repos: Seq[Repo] = getRepos(accessToken, urlStr).result()

    s"${repos.length} repos".>>
//    repos foreach (_.>>)

    ()
  }

  def getRepos(accessToken: AccessToken, urlStr: String): Future[Seq[Repo]] = (
    getRepos1(accessToken, urlStr, Vector.empty)
      map (_ recoverTotal (e => sys error s"Failed to read repos:\n${e.toFlatJson.pp}"))
  )

  def getRepos1(accessToken: AccessToken, urlStr: String, repos: Vector[Repo]): Future[JsResult[Seq[Repo]]] =
    (WS
      url urlStr
      withHeaders        "Accept" -> "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token $accessToken"
      get()
      flatMap { resp =>
        resp.json.validate[Seq[Repo]] match {
          case jsS @ JsSuccess(moreRepos, _) =>
            resp header "Link" flatMap getNextLink match {
              case Some(nextUrlStr) => getRepos1(accessToken, nextUrlStr, repos ++ moreRepos)
              case None             => Future successful jsS
            }
          case jsE: JsError                  =>
            jsE.errors foreach { case path -> errors =>
              val value = path.asSingleJson(resp.json)
              s"Error at ${path.toJsonString}, value: $value, errors:".>>
              errors foreach (err => f"${err.message}%35s : ${err.args.mkString("[","],[","]")}".>>)
            }
            Future successful jsE
        }
      }
    )

  def getNextLink(link: String) = """<(.+)>; rel="next"""".r findFirstMatchIn link map (_ group 1)
}

case class AccessToken(value: String) extends AnyVal {
  override def toString = value
}

// ISO-8601: YYYY-MM-DDTHH:MM:SSZ

case class Repo(
  name      : String,
  full_name : String,
  `private` : Boolean,
  fork      : Boolean,
  language  : Option[String]
)
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}
