package net.mox9.skithub

object T {
  val userAgent = UserAgent("dwijnand")

  val accessToken =
    sys.env get "GITHUB_API_TOKEN"  map AccessToken getOrElse (sys error "Need to set GITHUB_API_TOKEN")

  val connectionConfig = ConnectionConfig(userAgent, accessToken)

  val github = new GitHubClient(connectionConfig)

  def main(args: Array[String]): Unit = {
    val org = args.headOption getOrElse (sys error "Provide an org name")

    start()

    try {
      val repos -> elapsed = timed {
        github.orgs getRepos org await30s
      }
      repos pipe (rs => s"${rs.length} repos".>>)

      s"Took: ${elapsed.toHHmmssSSS}".>>
    } finally stop()
  }

  def newApp = new play.api.DefaultApplication(new java.io.File("."), this.getClass.getClassLoader, None, play.api.Mode.Dev)

  def start() = play.api.Play start newApp
  def stop()  = play.api.Play.stop()
}
