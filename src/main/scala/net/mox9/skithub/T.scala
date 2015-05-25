package net.mox9.skithub

import play.api.{ Mode, Play, DefaultApplication }
import play.api.libs.ws._

import java.io.File

object T {
  def main(args: Array[String]): Unit = {
    val githubToken = sys.env get "GITHUB_API_TOKEN" getOrElse (sys error "Need to set GITHUB_API_TOKEN")
    val org = args.head

    val appBaseDir = new File(".")
    val appClassLoader = this.getClass.getClassLoader
    implicit val app = new DefaultApplication(appBaseDir, appClassLoader, None, Mode.Dev) doto Play.start

    val reposFut = (WS
      url s"https://api.github.com/orgs/$org/repos"
      withHeaders        "Accept" ->  "application/vnd.github.v3+json"
      withHeaders "Authorization" -> s"token $githubToken"
      get()
    )

    val reposResp = reposFut.result()

    val json = reposResp.json
    // ISO-8601: YYYY-MM-DDTHH:MM:SSZ

    json.pp >>

    Play.stop()
  }
}
