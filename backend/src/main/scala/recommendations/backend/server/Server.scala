package recommendations.backend.server

import recommendations.backend.db.{DatabaseManager, DefaultDatabaseManager}
import recommendations.backend.recommendation.{DefaultRecommendationManager, RecommendationManager}
import recommendations.backend.server.ServerHandlers._
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import recommendations.backend.server.ServerHelper._
import io.vertx.scala.ext.web.handler.BodyHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}

import scala.concurrent.Future

class Server extends ScalaVerticle {

  private val dbManager: DatabaseManager = new DefaultDatabaseManager()
  private val recommendationManager: RecommendationManager = new DefaultRecommendationManager(dbManager.getMongoClient)

  override def startFuture(): Future[_] = {
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create())
    setupSessions(router, vertx, dbManager.getAuthProvider)
    setupRedirects(router, dbManager)
    setupRoutes(router)
    setupSecurity(router, vertx)
    createServer(router, vertx)
  }

  private def setupRoutes(router: Router): Unit = {
    router.get("/place/:placeId/reviews").handler(handleGetPlaceReviews(_, dbManager))
    router.get("/place/:placeId/average_stars").handler(handleGetAverageStars(_, isService = false, dbManager))
    router.post("/private/place/:placeId/review").handler(handlePostPlaceReview(_, dbManager))
    router.delete("/private/place/:placeId/review").handler(handleDeletePlaceReview(_, dbManager))
    router.get("/service/:serviceId/reviews").handler(handleGetServiceReviews(_, dbManager))
    router.get("/service/:serviceId/average_stars").handler(handleGetAverageStars(_, isService = true, dbManager))
    router.post("/private/service/:serviceId/review").handler(handlePostServiceReview(_, dbManager))
    router.delete("/private/service/:serviceId/review").handler(handleDeleteServiceReview(_, dbManager))
    router.get("/private/recommendations/service_category/:service_category/get")
      .handler(handleGetRecommendations(_, recommendationManager, executionContext))
    router.get("/service/:serviceId").handler(handleGetService(_, dbManager))
    router.get("/services/with_similar_name/:name").handler(handleSearchServices(_, dbManager))
    router.post("/private/service").handler(handlePostService(_, dbManager))
    router.get("/self/service_reviews")
      .handler(handleGetUserItemReviews(isRequestForServiceReviews = true, isCurrentUser = true)(_, dbManager))
    router.get("/self/place_reviews")
      .handler(handleGetUserItemReviews(isRequestForServiceReviews = false, isCurrentUser = true)(_, dbManager))
    router.get("/user/:userId/service_reviews")
      .handler(handleGetUserItemReviews(isRequestForServiceReviews = true, isCurrentUser = false)(_, dbManager))
    router.get("/user/:userId/place_reviews")
      .handler(handleGetUserItemReviews(isRequestForServiceReviews = false, isCurrentUser = false)(_, dbManager))
    router.get("/user/:userId/user_name").handler(handleGetUserName(_, dbManager))
    router.post("/user/:userName").handler(handlePostUser(_, dbManager))
    router.delete("/private/user").handler(handleDeleteUser(_, dbManager))
    router.post("/user/:userName/session").handler(handleLogin(_, dbManager))
    router.delete("/user/session").handler(handleLogout(_))
    router.post("/private/service/:serviceId/voteForWrongData")
      .handler(handleServiceVote(_, vote = false, dbManager))
    router.post("/private/service/:serviceId/voteForCorrectData")
      .handler(handleServiceVote(_, vote = true, dbManager))
    router.head("/private/service/:serviceId/vote").handler(checkVoteExistence(_, dbManager))
  }
}

object Main extends App {
  Vertx vertx() deployVerticle new Server()
}