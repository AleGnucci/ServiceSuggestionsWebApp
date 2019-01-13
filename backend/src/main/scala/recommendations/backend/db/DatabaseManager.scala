package recommendations.backend.db

import io.vertx.lang.scala.json.{JsonArray, JsonObject}
import io.vertx.scala.ext.auth.AuthProvider
import io.vertx.scala.ext.mongo.MongoClient
import io.vertx.scala.ext.web.RoutingContext
import recommendations.backend.common.ServiceVote

trait DatabaseManager {

  def getUserName(userId: Long, resultHandler: Option[JsonObject] => Unit): Unit

  def getPlaceReviews(placeId : Long, resultHandler : Option[JsonArray] => Unit)

  def addPlaceReview(review : JsonObject, userId : Long, placeId: Long, resultHandler : Boolean => Unit)

  def deletePlaceReview(placeId : Long, userId : Long, resultHandler : Boolean => Unit)

  def getServiceReviews(serviceId : Long, resultHandler : Option[JsonArray] => Unit)

  def addServiceReview(review : JsonObject, userId : Long, serviceId: Long, resultHandler : Boolean => Unit)

  def deleteServiceReview(ServiceId : Long, userId : Long, resultHandler : Boolean => Unit)

  def getService(serviceId : Long, resultHandler : Option[JsonObject] => Unit)

  def addService(service: JsonObject, userId: Long, resultHandler: Boolean => Unit)

  def getItemReviewsByUser(userId: Long, isRequestForServiceReviews: Boolean, resultHandler: Option[JsonArray] => Unit)

  def addUser(userName : String, password : String, resultHandler : Boolean => Unit)

  def deleteUser(userName: String, resultHandler : Boolean => Unit)

  def login(userName : String, password : String, resultHandler : Boolean => Unit)
           (implicit context: RoutingContext)

  def searchServices(name: String, resultHandler : Option[JsonArray] => Unit)

  def getItemAverageStars(itemId: Long, isService: Boolean, resultHandler : Option[JsonObject] => Unit)

  def voteService(vote: ServiceVote, resultHandler: Boolean => Unit)

  def checkVotePresence(serviceId: Long, userId: Long,resultHandler: Boolean => Unit)

  def getMongoClient: MongoClient

  def getAuthProvider: AuthProvider
}
