package net.mox9.octokit

import scala.language.higherKinds
import scala.language.implicitConversions

trait PlayWsKitPre {
  @inline final type WSResponse = play.api.libs.ws.WSResponse
  @inline final type WSClient   = play.api.libs.ws.WSClient

  @inline final val WS = play.api.libs.ws.WS
}
