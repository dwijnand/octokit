package net.mox9.octokit

trait PlayWsKitPre {
  @inline final type WSClient   = play.api.libs.ws.WSClient
  @inline final type WSRequest  = play.api.libs.ws.WSRequest
  @inline final type WSResponse = play.api.libs.ws.WSResponse

  @inline final val WS = play.api.libs.ws.WS
}
