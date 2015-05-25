package net.mox9.skithub

import play.api.libs.ws._
import play.api.{ DefaultApplication, Mode, Play }

import java.io.File

object T {
  // ISO-8601: YYYY-MM-DDTHH:MM:SSZ

  case class AccessToken(value: String) extends AnyVal {
    override def toString = value
  }

  def main(args: Array[String]): Unit = {
    val accessToken =
      sys.env get "GITHUB_API_TOKEN" getOrElse (sys error "Need to set GITHUB_API_TOKEN") pipe AccessToken
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val appBaseDir = new File(".")
    val appClassLoader = this.getClass.getClassLoader

    Play.start(new DefaultApplication(appBaseDir, appClassLoader, None, Mode.Dev))

    try go(accessToken, org)
    finally Play.stop()
  }

  def go(accessToken: AccessToken, org: String): Unit = {
    import Play.current

    val reposFut = (WS
      url s"https://api.github.com/orgs/$org/repos"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token $accessToken"
      get()
    )

    val reposResp = reposFut.result()

    val json = reposResp.json

    val repos = json.validate[Seq[Repo]] recoverTotal
      (e => sys error s"Failed to read repos:\n${e.toFlatJson.pp}")

    s"${repos.length} repos".>>
    repos foreach (_.>>)

    ()
  }
}

case class Repo(
  name      : String,
  full_name : String,
  `private` : Boolean,
  fork      : Boolean,
  language  : String
)
object Repo {
  implicit val jsonFormat: JsonFormat[Repo] = Json.format[Repo]
}
