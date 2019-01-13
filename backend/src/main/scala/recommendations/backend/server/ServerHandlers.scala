package recommendations.backend.server

import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.{RoutingContext, Session}
import recommendations.backend.common.{ServiceVote, ServiceCategory}
import recommendations.backend.db.DatabaseManager
import recommendations.backend.recommendation.RecommendationManager
import recommendations.backend.server.HttpUtilities.{getContextData, respondOk, respondOkWithJson, respondWithCode}
import recommendations.backend.server.ServerHelper._

import scala.concurrent.ExecutionContext

object ServerHandlers {

  def handleGetPlaceReviews(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val placeId = getPlaceId
    doIfDefined(placeId, dbManager.getPlaceReviews(placeId.get, dbArrayResultHandler))
  }

  def handlePostPlaceReview(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val placeId = getPlaceId
    doIfDefined(placeId,
      handlePostItem((body, userId) => dbManager.addPlaceReview(body, userId, placeId.get, dbResultHandler)))
  }

  def handleDeletePlaceReview(implicit context: RoutingContext, dbManager: DatabaseManager): Unit =
    handleDelete(getPlaceId, (userId, placeId) => dbManager.deletePlaceReview(placeId, userId, dbResultHandler))

  def handleGetServiceReviews(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val serviceId = getServiceId
    doIfDefined(serviceId, dbManager.getServiceReviews(serviceId.get, dbArrayResultHandler))
  }

  def handlePostServiceReview(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val serviceId = getServiceId
    doIfDefined(serviceId,
      handlePostItem((body, userId) => dbManager.addServiceReview(body, userId, serviceId.get, dbResultHandler)))
  }

  def handleDeleteServiceReview(implicit context: RoutingContext, dbManager: DatabaseManager): Unit =
    handleDelete(getServiceId,
      (userId, serviceId) => dbManager.deleteServiceReview(serviceId, userId, dbResultHandler))

  def handleGetRecommendations(implicit context: RoutingContext, recommendationManager: RecommendationManager,
                               executionContext: ExecutionContext): Unit = {
    val userId: Option[Long] = getUserIdFromSession
    val serviceCategory = getContextData("service_category")
      .flatMap(data => ServiceCategory.values.find(_.toString == data))
    doIfDefined(userId,
      doIfDefined(serviceCategory, recommendationManager.recommendServices(userId.get, serviceCategory.get,
        res => respondOkWithJson(Left(Json.obj(("recommendations", res)))))))
  }

  def handleGetService(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val serviceId = getServiceId
    doIfDefined(serviceId, dbManager.getService(serviceId.get, dbResultHandler))
  }

  def handleSearchServices(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val name = getContextData("name")
    doIfDefined(name, dbManager.searchServices(name.get, dbArrayResultHandler))
  }

  def handleGetUserItemReviews(isRequestForServiceReviews: Boolean, isCurrentUser: Boolean)
                              (implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val userId: Option[Long] = if (isCurrentUser) getUserIdFromSession else getUserIdFromContext
    doIfDefined(userId, dbManager.getItemReviewsByUser(userId.get, isRequestForServiceReviews,  dbArrayResultHandler))
  }

  def handleGetUserName(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val userId = getUserIdFromContext
    doIfDefined(userId, dbManager.getUserName(userId.get, dbResultHandler))
  }

  def handlePostUser(implicit context: RoutingContext, dbManager: DatabaseManager): Unit =
    doIfValidUserNameAndBody((userName, body) =>
      dbManager.addUser(userName, body.getString("password"), dbResultHandler))

  def handleDeleteUser(implicit context: RoutingContext, dbManager: DatabaseManager): Unit = {
    val userName = getUserNameFromSession
    if(userName.isDefined){
      dbManager.deleteUser(userName.get, res =>  {
        if(res){
          handleLogout
        } else{
          respondWithCode(400)
        }
      })
    } else{
      respondWithCode(403)
    }
  }

  def handlePostService(implicit context: RoutingContext, dbManager: DatabaseManager): Unit =
    handlePostItem((body, userId) => dbManager.addService(body, userId, dbResultHandler))

  def handleLogout(implicit context: RoutingContext): Unit = {
    context.clearUser
    context.session().fold(() => {})((session:Session) => () => session.destroy())
    respondOk
  }

  def handleLogin(implicit context: RoutingContext, dbManager: DatabaseManager): Unit =
    doIfValidUserNameAndBody((userName, body) =>
      dbManager.login(userName, body.getString("password"),
        if(_) respondOk else respondWithCode(401)))

  def handleGetAverageStars(implicit context: RoutingContext, isService: Boolean, dbManager: DatabaseManager): Unit = {
    val itemId = getPlaceId orElse getServiceId
    doIfDefined(getPlaceId orElse getServiceId, dbManager.getItemAverageStars(itemId.get, isService, dbResultHandler))
  }

  def handleServiceVote(implicit context: RoutingContext, vote: Boolean, dbManager: DatabaseManager): Unit = {
    val serviceId = getServiceId
    val userId = getUserIdFromSession
    val itemVote = ServiceVote(userId.get, serviceId.get, vote)
    doIfDefined(serviceId, doIfDefined(userId, dbManager.voteService(itemVote, dbResultHandler)))
  }

  def checkVoteExistence(implicit context: RoutingContext, dbManager: DatabaseManager) = {
    val serviceId = getServiceId
    val userId = getUserIdFromSession
    doIfDefined(serviceId, doIfDefined(userId,
      dbManager.checkVotePresence(serviceId.get, userId.get, dbHeadResultHandler)))
  }
}
