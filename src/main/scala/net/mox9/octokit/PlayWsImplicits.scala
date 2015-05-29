package net.mox9.octokit

import scala.language.higherKinds
import scala.language.implicitConversions

trait PlayWsImplicits {
  @inline type WSResponse = play.api.libs.ws.WSResponse

  @inline val WS = play.api.libs.ws.WS
}
