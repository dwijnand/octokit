package net.mox9

// TODO: Rename to octokit.scala?
// TODO: tabulate/Tabulate class/.tt ext method
// TODO: tabulate implicits trait
// TODO: look into NSME: commit 612b11957b
// TODO: Migrate to Play 2.4
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Make aliases final?
// TODO: Switch to WSClient
// TODO: Handle HTTP codes. Abstraction?
package object skithub extends ScalaImplicits
  with PlayFunctionalImplicits with PlayJsonImplicits with PlayWsImplicits
