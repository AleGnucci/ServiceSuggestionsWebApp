package recommendations.backend.server

import io.vertx.core.http.HttpMethod
import recommendations.backend.db.DatabaseManager
import recommendations.backend.server.HttpUtilities.{getContextData, respondOk, respondWithCode}
import recommendations.backend.server.HttpUtilities._
import io.vertx.lang.scala.json.{JsonArray, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.auth.AuthProvider
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.vertx.scala.ext.web.handler._
import io.vertx.scala.ext.web.sstore.LocalSessionStore
import recommendations.backend.common.names.SessionNames

import scala.concurrent.{ExecutionContext, Future}

object ServerHelper {

  def setupSecurity(router: Router, vertx: Vertx): Unit = {
    setupCors(router)
    router.route().handler(SessionHandler
      .create(LocalSessionStore.create(vertx))
      .setCookieHttpOnlyFlag(true)
      .setCookieSecureFlag(true))
    router.route.handler(ctx => ctx.response
      .putHeader("Cache-Control", "no-store, no-cache")
      .putHeader("X-Content-Type-Options", "nosniff")
      .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
      .putHeader("X-Download-Options", "noopen")
      .putHeader("X-XSS-Protection", "1; mode=block")
      .putHeader("X-FRAME-OPTIONS", "DENY"))
    router.route.handler(CSRFHandler.create("Ws)Xx\"7Qa1AZklco9}'@Tt8;~m$e&~3`[b'=qPC&;6omq#znX_EsIr]JS?@;}6kM"))
  }

  private def setupCors(router: Router): Unit = {
    val allowedHeaders = Array("x-requested-with", "Access-Control-Allow-Origin", "origin", "Content-Type", "accept",
      "X-PINGARUNER")
    router.route.handler(CorsHandler.create(".*").allowedHeaders(collection.mutable.Set(allowedHeaders.toSeq:_*))
      .allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.POST)
      .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
      .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
      .allowCredentials(true)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Content-Type"))
  }

  def setupSessions(router: Router, vertx: Vertx, authProvider : AuthProvider): Unit = {
    router.route().handler(CookieHandler.create())
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))
    router.route.handler(UserSessionHandler.create(authProvider))
  }

  def setupRedirects(router: Router, dbManager: DatabaseManager): Unit = {
    val handler = RedirectAuthHandler.create(dbManager.getAuthProvider, "/")
    val route = router.route("/private/*")
    route.method(HttpMethod.GET).handler(handler)
    route.method(HttpMethod.POST).handler(handler)
    route.method(HttpMethod.DELETE).handler(handler)
  }

  def createServer(router: Router, vertx: Vertx)(implicit context: ExecutionContext): Future[_] =
    vertx.createHttpServer()
      .requestHandler(router.accept(_))
      .listenFuture(8080, "0.0.0.0")
      .map { _ => println("Service started") }

  def getPlaceId(implicit context: RoutingContext): Option[Long] =
    toLongOption(getContextData("placeId"))

  def handlePostItem(action: (JsonObject, Long) => Unit)(implicit context: RoutingContext): Unit = {
    val body = context.getBodyAsJson()
    val userId = getUserIdFromSession
    if (body.isDefined && userId.isDefined) {
      action(body.get, userId.get)
    } else {
      respondWithCode(400)
    }
  }

  def getUserIdFromSession(implicit context: RoutingContext): Option[Long] =
    context.session().map(session => session.get(SessionNames.USER_ID).asInstanceOf[Long])

  def getUserNameFromSession(implicit context: RoutingContext): Option[String] =
    context.session().map(session => session.get(SessionNames.USER_NAME).asInstanceOf[String])

  def doIfDefined(id: Option[_], action: => Unit)(implicit context: RoutingContext): Unit =
    if (id.isDefined) {
      action
    } else respondWithCode(400)

  private def isAllDigits(string: String): Boolean =
    string.forall(Character.isDigit)

  def dbHeadResultHandler(success: Boolean)(implicit context: RoutingContext): Unit =
    if (success) respondOk else respondWithCode(404)

  def dbResultHandler(success: Boolean)(implicit context: RoutingContext): Unit =
    if (success) respondOk else respondWithCode(400)

  def dbResultHandler(result: Option[JsonObject])(implicit context: RoutingContext): Unit =
    if(result.isDefined) respondOkWithJson(Left(result.get)) else respondWithCode(400)

  def dbArrayResultHandler(result: Option[JsonArray])(implicit context: RoutingContext): Unit =
    if(result.isDefined) respondOkWithJson(Right(result.get)) else respondWithCode(400)

  def getServiceId(implicit context: RoutingContext): Option[Long] =
    toLongOption(getContextData("serviceId"))

  private def toLongOption(stringOption: Option[String]): Option[Long] =
    stringOption.filter(id => isAllDigits(id)).map(id => id.toLong)

  def getUserName(implicit context: RoutingContext): Option[String] =
    getContextData("userName")

  def doIfValidUserNameAndBody(action: (String, JsonObject) => Unit)(implicit context: RoutingContext): Unit = {
    val userName = getUserName
    val body = context.getBody()
    if (userName.isDefined && body.isDefined && body.get.getBytes.length>0) {
      action.apply(userName.get, body.get.toJsonObject)
    } else {
      respondWithCode(400)
    }
  }

  def handleDelete(itemId: Option[Long], action: (Long, Long)=> Unit)(implicit context: RoutingContext): Unit =
    getUserIdFromSession match {
      case Some(userId) => doIfDefined(itemId, action(userId, itemId.get))
      case None => respondWithCode(403)
    }
}