package net.mox9

// TODO: Tabulate class
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Handle HTTP codes. Abstraction?
// TODO: Figure out how to auto-stop on Ctrl-D in REPL
// TODO: Handle IO error nicer then stacktrace in your face?
// TODO: Add BasicAuth support?
// TODO: Add query params to API calls
// TODO: Add support for using Resp ETag and sending as If-None-Match
// TODO: Compare data field types with other octokits
// TODO: RateLimitClient https://developer.github.com/v3/rate_limit/
// TODO: Add caching on ETag
package object octokit
  extends ScalaKitPre
     with TabularKitPre
     with AkkaKitPre
     with PlayFunctionalKitPre with PlayJsonKitPre with PlayWsKitPre
