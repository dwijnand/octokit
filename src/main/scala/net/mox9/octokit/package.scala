package net.mox9

// TODO: Tabulate class
// TODO: Migrate to Play 2.4
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Make aliases final?
// TODO: Switch to WSClient
// TODO: Handle HTTP codes. Abstraction?
package object octokit extends ScalaImplicits
  with TabularImplicits
  with PlayFunctionalImplicits with PlayJsonImplicits with PlayWsImplicits
