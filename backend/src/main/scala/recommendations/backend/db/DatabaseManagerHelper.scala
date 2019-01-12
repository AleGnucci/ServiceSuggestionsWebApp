package recommendations.backend.db

import java.util.NoSuchElementException

import recommendations.backend.common._
import io.vertx.core.AsyncResult
import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.mongo.{FindOptions, MongoClient}
import io.vertx.scala.ext.web.RoutingContext
import recommendations.backend.common.names._

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object DatabaseManagerHelper {

  def onGetComplete(res : Try[mutable.Buffer[JsonObject]], resultHandler: Option[JsonArray] => Unit): Unit =
    res match{
      case Success(result) => resultHandler(Some(Json.arr(result)))
      case Failure(_) => resultHandler(None)
    }

  def isJsonObjectValid(jsonObject: JsonObject) : Boolean =
    jsonObject.getMap.entrySet().stream()
      .noneMatch(field => field.getValue.isInstanceOf[JsonObject] || field.getValue.isInstanceOf[JsonArray])

  def onComplete(tryObj : Try[_], resultHandler: Boolean => Unit) : Unit = tryObj match {
    case Success(_) => resultHandler.apply(true)
    case Failure(failure) =>
      println(failure)
      resultHandler.apply(false)
  }

  def setupMongoClient(vertx: Vertx) : MongoClient = {
    val mongoConfig: JsonObject = new JsonObject()
      .put("connection_string", "mongodb://localhost:27017")
      .put("db_name", "recommendations")
    //Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE)
    MongoClient.createShared(vertx, mongoConfig)
  }

  def getCompleteReview(review : JsonObject, userId : Long) : JsonObject =
    if(isAServiceReview(review)){
      review.put(ServiceReviewFields.USER_ID, userId)
      review.put(ServiceReviewFields.DATE, getTimeStamp)
    } else{
      review.put(PlaceReviewFields.USER_ID, userId)
      review.put(PlaceReviewFields.DATE, getTimeStamp)
    }

  def isAServiceReview(review : JsonObject): Boolean = review.containsKey(ServiceReviewFields.SERVICE_ID)

  private def getTimeStamp: Long = System.currentTimeMillis / 1000

  def setSessionDataIfSuccess(result: AsyncResult[mutable.Buffer[JsonObject]], resultHandler: Boolean => Unit,
                              userName: String) (implicit context: RoutingContext): Unit = {
    if(result.succeeded()) {
      context.session().get
        .put(SessionNames.USER_ID, result.result().iterator.next().getLong(UserIdFields.ID))
        .put(SessionNames.USER_NAME, userName)
    }
    resultHandler(result.succeeded())
  }

  def isServiceValid(service: JsonObject): Boolean = {
    val result = service.containsKey(ServiceFields.NAME) && service.containsKey(ServiceFields.CATEGORY) &&
      service.containsKey(ServiceFields.PLACE_ID) && service.containsKey(ServiceFields.DESCRIPTION) &&
      service.size() == 4 && isJsonObjectValid(service)
    try {
      ServiceCategory.withName(service.getString(ServiceFields.CATEGORY))
    } catch{
      case _: NoSuchElementException => return false
    }
    result
  }

  def addUserId(mongoClient: MongoClient, collection: String, userName: String, resultHandler: Boolean => Unit)
               (implicit context: ExecutionContext): Unit =
    addWithIncrementedId(mongoClient, Json.obj((UserIdFields.USERNAME, userName)), collection, resultHandler)

  /**
  * Works for every db document that has a field called "id"
  * */
  def addWithIncrementedId(mongoClient: MongoClient, item : JsonObject, collection : String,
                           resultHandler : Boolean => Unit)(implicit context: ExecutionContext): Unit = {
    val findOptions = FindOptions.fromJson(Json.obj(("sort", Json.obj(("id", -1))), ("limit", "1")))
    mongoClient.findWithOptionsFuture(collection, Json.obj(), findOptions).onComplete(result => {
      if(result.isSuccess){
        mongoClient.insertFuture(collection, item.put("id", result.get.iterator.next().getLong("id")+1))
          .onComplete(onComplete(_, resultHandler))
      } else{
        resultHandler(false)
      }
    })
  }


  def checkReview(review: JsonObject, resultHandler: Boolean => Unit) : Boolean = {
    if(!isReviewValid(review)){
      resultHandler.apply(false)
      return false
    }
    true
  }

  private def isReviewValid(review: JsonObject) : Boolean =
    (review.containsKey(ServiceReviewFields.STARS) && review.containsKey(ServiceReviewFields.COMMENT)
      && review.getInteger(ServiceReviewFields.STARS)>=0 && review.getInteger(ServiceReviewFields.STARS)<=5 ||
      review.containsKey(PlaceReviewFields.STARS) && review.containsKey(PlaceReviewFields.COMMENT) &&
        review.getInteger(PlaceReviewFields.STARS)>=0 && review.getInteger(PlaceReviewFields.STARS)<=5) &&
      review.size() == 2 && isJsonObjectValid(review)

  def getAverageStars(itemId: Long, isService: Boolean, mongoClient: MongoClient,
                      resultHandler: Option[JsonObject] => Unit)(implicit context: ExecutionContext): Unit = {
    val command = getAverageStarsCommand(itemId, isService)
    mongoClient.runCommandFuture("aggregate", command).onComplete{
      case Success(result) =>
        val resultArray = result.getJsonObject("cursor").getJsonArray("firstBatch")
        resultHandler(Some(if(resultArray.size() > 0) resultArray.getJsonObject(0) else Json.obj(("stars", null))))
      case Failure(error) => {
        resultHandler(None)
        println(error)
      }
    }
  }

  private def getAverageStarsCommand(itemId: Long, isService: Boolean): JsonObject = {
    val collection = if(isService) CollectionNames.SERVICE_REVIEWS else CollectionNames.PLACE_REVIEWS
    val itemIdFieldName = if(isService) ServiceReviewFields.SERVICE_ID else PlaceReviewFields.PLACE_ID
    Json.obj(("aggregate", collection), ("pipeline", Json.arr(Json.obj(("$match", Json.obj((itemIdFieldName, itemId)))),
      Json.obj(("$group", Json.obj(("_id", null), ("averageStars", Json.obj(("$avg", "$"+PlaceReviewFields.STARS)))))))),
    ("cursor", Json.obj(("batchSize", 1))))
  }

  def removeServiceIfNeeded(mongoClient: MongoClient, serviceId: Long): Unit ={
    mongoClient.count(CollectionNames.USERS, Json.obj(), usersCount => {
      val wrongDataVotesQuery = Json.obj((ServiceVoteFields.SERVICE_ID, serviceId),
        (ServiceVoteFields.IS_VOTE_FOR_REMOVAL, true))
      mongoClient.count(CollectionNames.SERVICE_REMOVAL_VOTES, wrongDataVotesQuery, wrongDataVotesCount => {
        val correctDataVotesQuery = wrongDataVotesQuery.put(ServiceVoteFields.IS_VOTE_FOR_REMOVAL, false)
        mongoClient.count(CollectionNames.SERVICE_REMOVAL_VOTES, correctDataVotesQuery, correctDataVotesCount => {
          val correctDataVotes = if(correctDataVotesCount.result() == 0) 1 else correctDataVotesCount.result()
          if(wrongDataVotesCount.result()/correctDataVotes>10 && wrongDataVotesCount.result()>usersCount.result()/20) {
            removeService(mongoClient, serviceId)
          }
        })
      })
    })
  }

  private def removeService(mongoClient: MongoClient, serviceId: Long): Unit ={
    mongoClient.remove(CollectionNames.SERVICES, Json.obj((ServiceFields.ID, serviceId)), _ => {})
    mongoClient.remove(CollectionNames.SERVICE_REVIEWS, Json.obj((ServiceReviewFields.SERVICE_ID, serviceId)), _ => {})
    val removeVotesQuery = Json.obj((ServiceVoteFields.SERVICE_ID, serviceId))
    mongoClient.remove(CollectionNames.SERVICE_REMOVAL_VOTES, removeVotesQuery, _ => {})
  }
}