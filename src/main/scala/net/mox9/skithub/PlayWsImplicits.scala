package net.mox9.skithub

import scala.language.higherKinds
import scala.language.implicitConversions

trait PlayWsImplicits {
  @inline type WSResponse = play.api.libs.ws.WSResponse

  @inline val WS = play.api.libs.ws.WS
}
