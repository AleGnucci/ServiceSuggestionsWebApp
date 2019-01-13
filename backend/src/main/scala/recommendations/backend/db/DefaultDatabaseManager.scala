package recommendations.backend.db

import recommendations.backend.db.authentication.{AuthManager, DefaultAuthManager}
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.{Json, JsonArray}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.mongo.MongoClient
import io.vertx.scala.ext.web.RoutingContext
import recommendations.backend.db.DatabaseManagerHelper._
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.auth.AuthProvider
import recommendations.backend.common.names.CollectionNames._
import recommendations.backend.common._
import recommendations.backend.common.names._
import recommendations.backend.db.util.{DefaultDocumentInserter, DefaultDocumentSearcher, DefaultReviewGetter}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class DefaultDatabaseManager extends DatabaseManager {

  private val vertx = Vertx.vertx()
  implicit private val executionContext : ExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())
  private val mongoClient = setupMongoClient(vertx)
  private val authManager: AuthManager  = new DefaultAuthManager(mongoClient, vertx)

  override def getPlaceReviews(placeId: Long, resultHandler: Option[JsonArray]=> Unit): Unit =
    new DefaultReviewGetter(mongoClient, resultHandler).getReviews(PLACE_REVIEWS, PlaceReviewFields.PLACE_ID, placeId)

  private def handleGet(itemId: Long, resultHandler: Option[JsonArray] => Unit,
                        collection : String, idName : String): Unit =
    mongoClient.findFuture(collection, Json.obj((idName, itemId))).onComplete(onGetComplete(_, resultHandler))

  override def addPlaceReview(review: JsonObject, userId : Long, placeId: Long,
                              resultHandler: Boolean => Unit): Unit = {
    if(!checkReview(review, resultHandler)){
      return
    }
    val reviewToSave = review.put(PlaceReviewFields.PLACE_ID, placeId)
    addReview(reviewToSave, userId, PLACE_REVIEWS, resultHandler)
  }

  private def addReview(review: JsonObject, userId : Long, collection : String,
                        resultHandler: Boolean => Unit): Unit = {
    if(isAServiceReview(review)){
      new DefaultDocumentInserter(mongoClient, SERVICES, SERVICE_REVIEWS, resultHandler)
        .addReviewIfOtherDocumentExists(ServiceFields.ID, review.getLong(ServiceReviewFields.SERVICE_ID), review, userId)
    } else {
      new DefaultDocumentInserter(mongoClient, null, PLACE_REVIEWS, resultHandler)
        .addReview(review, userId)
    }
  }

  override def deletePlaceReview(placeId: Long, userId: Long, resultHandler: Boolean => Unit): Unit =
    mongoClient.removeFuture(PLACE_REVIEWS, Json.obj((PlaceReviewFields.PLACE_ID, placeId),
      (PlaceReviewFields.USER_ID, userId)))
      .onComplete(onComplete(_, resultHandler))

  override def getServiceReviews(serviceId: Long, resultHandler: Option[JsonArray] => Unit): Unit =
    new DefaultReviewGetter(mongoClient, resultHandler)
      .getReviews(SERVICE_REVIEWS, ServiceReviewFields.SERVICE_ID, serviceId)

  override def addServiceReview(review: JsonObject, userId : Long, serviceId: Long,
                                resultHandler: Boolean => Unit): Unit = {
    if(!checkReview(review, resultHandler)){
      return
    }
    val reviewToSave = review.put(ServiceReviewFields.SERVICE_ID, serviceId)
    addReview(reviewToSave, userId, SERVICE_REVIEWS, resultHandler)
  }

  override def deleteServiceReview(serviceId: Long, userId: Long, resultHandler: Boolean => Unit): Unit =
    mongoClient.removeFuture(SERVICE_REVIEWS,
      Json.obj((ServiceReviewFields.SERVICE_ID, serviceId), (ServiceReviewFields.USER_ID, userId)))
      .onComplete(onComplete(_, resultHandler))

  override def addService(service: JsonObject, userId: Long, resultHandler: Boolean => Unit): Unit = {
    if(!isServiceValid(service)){
      resultHandler.apply(false)
      return
    }
    addWithIncrementedId(mongoClient, service, SERVICES, resultHandler)
  }

  override def getItemReviewsByUser(userId: Long, isRequestForServiceReviews: Boolean,
                                    resultHandler: Option[JsonArray] => Unit): Unit = {
    val collection = if(isRequestForServiceReviews) SERVICE_REVIEWS else PLACE_REVIEWS
    val idFieldName = if(isRequestForServiceReviews) ServiceReviewFields.USER_ID else PlaceReviewFields.USER_ID
    new DefaultReviewGetter(mongoClient, resultHandler).getReviews(collection, idFieldName, userId)
  }

  override def getUserName(userId: Long, resultHandler: Option[JsonObject] => Unit): Unit =
    mongoClient.findFuture(USER_IDS, Json.obj((UserIdFields.ID, userId))).onComplete({
      case Success(mutable.Buffer(json)) => resultHandler(Some(json))
      case Failure(_) => resultHandler(None)
    })

  override def addUser(userName: String, password: String, resultHandler: Boolean => Unit): Unit =
    authManager.addUser(userName, password, success => {
      if(!success){
        resultHandler(false)
        return
      }
      addUserId(mongoClient, USER_IDS, userName, resultHandler)
    })

  override def deleteUser(userName: String, resultHandler: Boolean => Unit): Unit = {
    val queryAndUpdate = Json.obj((UserFields.NAME, userName))
    mongoClient.replaceFuture(USERS, queryAndUpdate, queryAndUpdate)
      .onComplete(onComplete(_, resultHandler))
  }

  override def login(userName: String, password : String, resultHandler: Boolean => Unit)
                    (implicit context: RoutingContext): Unit = {
    authManager.login(userName, password, res => {
      if(res.isDefined){
        mongoClient.find(USER_IDS, Json.obj((UserIdFields.USERNAME, userName)), result => {
          setSessionDataIfSuccess(result, resultHandler, userName)
        })
      } else{
        resultHandler(false)
      }
    })
  }

  private def getItem(itemId: Long, collection: String, idFieldName: String,
                      resultHandler: Option[JsonObject] => Unit): Unit =
    handleGet(itemId, jsonArray => {
      val jsonObject = if(jsonArray.isDefined && jsonArray.get.size()>0 &&
        !jsonArray.get.getJsonArray(0).isEmpty) {
        Some(jsonArray.get.getJsonArray(0).getJsonObject(0))
      } else None
      if(jsonObject.isDefined){
        getAverageStars(itemId, collection.equals(CollectionNames.SERVICES), mongoClient,
          stars => {
            resultHandler(Some(Json.obj(("item", jsonObject.get),
              ("stars", stars.getOrElse(Json.obj(("averageStars", 0))).getInteger("averageStars")))))
          })
      } else{
        resultHandler(None)
      }
    }, collection, idFieldName)

  override def getService(serviceId: Long, resultHandler: Option[JsonObject] => Unit): Unit =
    getItem(serviceId, SERVICES, ServiceFields.ID, resultHandler)

  override def searchServices(name: String, resultHandler: Option[JsonArray] => Unit): Unit =
    new DefaultDocumentSearcher(mongoClient, resultHandler).search(SERVICES, ServiceFields.NAME, name)

  override def getItemAverageStars(itemId: Long, isService: Boolean, resultHandler: Option[JsonObject] => Unit): Unit =
    getAverageStars(itemId, isService, mongoClient, resultHandler)

  override def voteService(vote: ServiceVote, resultHandler: Boolean => Unit): Unit = {
    val jsonVote = Json.obj((ServiceVoteFields.SERVICE_ID, vote.serviceId), (ServiceVoteFields.USER_ID, vote.userId),
      (ServiceVoteFields.IS_VOTE_FOR_REMOVAL, !vote.vote))
    mongoClient.insertFuture(CollectionNames.SERVICE_REMOVAL_VOTES, jsonVote).onComplete(onComplete(_, res => {
      resultHandler(res)
      removeServiceIfNeeded(mongoClient, vote.serviceId)
    }))
  }

  override def checkVotePresence(serviceId: Long, userId: Long, resultHandler: Boolean => Unit): Unit = {
    val query = Json.obj((ServiceVoteFields.USER_ID, userId), (ServiceVoteFields.SERVICE_ID, serviceId))
    mongoClient.count(CollectionNames.SERVICE_REMOVAL_VOTES, query, count => resultHandler(count.result()>0))
  }

  override def getMongoClient: MongoClient = mongoClient

  override def getAuthProvider: AuthProvider = authManager.getAuthProvider
}