package net.mox9.octokit

import scala.language.higherKinds
import scala.language.implicitConversions

trait PlayWsImplicits {
  @inline type WSResponse = play.api.libs.ws.WSResponse
  @inline type WSClient   = play.api.libs.ws.WSClient

  @inline val WS = play.api.libs.ws.WS
}
