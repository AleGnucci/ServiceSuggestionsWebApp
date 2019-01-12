package recommendations.backend.db.util

import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import io.vertx.scala.ext.mongo.MongoClient
import recommendations.backend.common.names.CollectionNames.USER_IDS
import recommendations.backend.common.names.{ServiceReviewFields, UserIdFields}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class DefaultReviewGetter(mongoClient: MongoClient, resultHandler: Option[JsonArray] => Unit) extends ReviewGetter {

  override def getReviews(collection: String, fieldName: String, fieldValue: Long)
                         (implicit context: ExecutionContext): Unit = {
    val reviewArray = Json.arr()
    mongoClient.findFuture(collection, Json.obj((fieldName, fieldValue)))
      .onComplete({
        case Success(reviews) => handleSuccessReviews(reviewArray, reviews)
        case Failure(_) => resultHandler(None)
      })
  }

  private def handleSuccessReviews(reviewArray: JsonArray, reviews: mutable.Buffer[JsonObject])
                                  (implicit context: ExecutionContext): Unit = {
    if(reviews.isEmpty){
      resultHandler(Some(Json.arr()))
    }
    reviews.foreach(review =>
      mongoClient.findFuture(USER_IDS, Json.obj((UserIdFields.ID, review.getLong(ServiceReviewFields.USER_ID))))
        .onComplete(res => {
          mergeReviewWithUser(res, reviewArray, review)
          returnIfCompleted(reviewArray, reviews)
        }))
  }

  private def mergeReviewWithUser(res: Try[mutable.Buffer[JsonObject]],
                          reviewArray: JsonArray, review: JsonObject): Unit = {
    if(res.isFailure){
      resultHandler(None)
      return
    }
    reviewArray.add(review.mergeIn(res.get.iterator.next()))
  }

  private def returnIfCompleted(reviewArray: JsonArray, reviews: mutable.Buffer[JsonObject]): Unit = {
    if(reviewArray.size() == reviews.size){
      resultHandler(Some(reviewArray))
    }
  }
}
