package net.mox9

// TODO: Tabulate class
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Make aliases final?
// TODO: Handle HTTP codes. Abstraction?
// TODO: Rename XImplicits to XKitPre, including SbtKitPre
package object octokit extends ScalaImplicits
  with TabularImplicits
  with AkkaImplicits
  with PlayFunctionalImplicits with PlayJsonImplicits with PlayWsImplicits
