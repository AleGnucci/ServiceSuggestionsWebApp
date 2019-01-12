package recommendations.backend.server

import io.vertx.core.http.HttpMethod.{DELETE, HEAD, POST}
import io.vertx.lang.scala.json.JsonObject
import io.vertx.lang.scala.json.JsonArray
import io.vertx.scala.ext.web.RoutingContext

object HttpUtilities {

  def getContextData(urlParameter: String)(implicit context: RoutingContext): Option[String] =
    context.request.getParam(urlParameter)

  def respondOk(implicit context: RoutingContext): Unit = {
    var response = context.response
    context.request.method match {
      case HEAD => response = response.setStatusCode(200)
      case POST => response = response.setStatusCode(204).putHeader("Location", context.request.absoluteURI)
      case DELETE => response = response.setStatusCode(204)
      case _ => throw new IllegalStateException("Responding ok without body in wrong http request method")
    }
    addCorsHeaders
    response.end
  }

  /**
    * To be used in GET method handlers
    **/
  def respondOkWithJson(body: Either[JsonObject, JsonArray])(implicit context: RoutingContext): Unit = {
    addCorsHeaders
    context.response
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(if(body.isLeft) body.left.get.encodePrettily() else body.right.get.encodePrettily())
  }

  def respondWithCode(statusCode: Int)(implicit context: RoutingContext): Unit = {
    addCorsHeaders
    context.response.setStatusCode(statusCode).end
  }

  def addCorsHeaders(implicit context: RoutingContext): Unit = {
    val clientAddress = context.request().headers().get("Origin")
    if(clientAddress.isEmpty){
      return
    }
    context.response().putHeader("Access-Control-Allow-Origin", clientAddress.get)
    context.response().putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE")
    context.response().putHeader("Access-Control-Allow-Headers", "accept, authorization, content-type, Origin, X-Requested-With")
    context.response().putHeader("Access-Control-Allow-Credentials", true.toString)
  }

  def isCodeSuccessful(statusCode: Int): Boolean =
    statusCode / 100 == 2
}
