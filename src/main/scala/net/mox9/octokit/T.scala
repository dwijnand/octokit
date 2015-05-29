package net.mox9.octokit

object T {
  val userAgent = UserAgent("dwijnand")

  val accessToken =
    sys.env get "GITHUB_API_TOKEN"  map AccessToken getOrElse (sys error "Need to set GITHUB_API_TOKEN")

  val connectionConfig = ConnectionConfig(userAgent, accessToken)

  private def newUnstartedApp() =
    new play.api.DefaultApplication(
      new java.io.File("."), this.getClass.getClassLoader, None, play.api.Mode.Dev)

  def newApp() = newUnstartedApp() doto play.api.Play.start

  def stop()  = play.api.Play.stop()

  def newGithub(implicit app: play.api.Application) = new GitHubClient(WS.client, connectionConfig)

  def github() = newGithub(newApp())

  def main(args: Array[String]): Unit = {
    val org = args.headOption getOrElse (sys error "Provide an org name")

    val github = T.github()

    try {
      val repos -> elapsed = timed {
        github.orgs getRepos org await30s
      }
      repos pipe (rs => s"${rs.length} repos".>>)

      s"Took: ${elapsed.toHHmmssSSS}".>>
    } finally stop()
  }
}
