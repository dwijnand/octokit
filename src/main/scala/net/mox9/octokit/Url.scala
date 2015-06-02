package net.mox9.octokit

import java.net.URL

final class Url private (override val toString: String) extends AnyVal {
  def toURL: URL = new URL(toString)
}

object Url extends (String => Try[Url]) {
  def apply(s: String): Try[Url] = Try(new Url(s)) map (_.toString) map (new Url(_))
}
