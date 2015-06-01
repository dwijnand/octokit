package net.mox9

// TODO: Tabulate class
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Handle HTTP codes. Abstraction?
// TODO: Figure out how to auto-stop on Ctrl-D in REPL
// TODO: Enable higherKind/implicitConversions/more? in scalac opts
// TODO: Handle IO error nicer then stacktrace in your face?
// TODO: Add BasicAuth support?
// TODO: Add query params to API calls
package object octokit
  extends ScalaKitPre
     with TabularKitPre
     with AkkaKitPre
     with PlayFunctionalKitPre with PlayJsonKitPre with PlayWsKitPre
