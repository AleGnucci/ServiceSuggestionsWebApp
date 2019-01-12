package recommendations.backend.db.util

import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.mongo.MongoClient
import recommendations.backend.db.DatabaseManagerHelper.getCompleteReview
import recommendations.backend.db.DatabaseManagerHelper.onComplete

import scala.concurrent.ExecutionContext

class DefaultDocumentInserter(mongoClient: MongoClient, collectionToSearchIn: String, collectionToAddTo: String,
                              resultHandler: Boolean => Unit) extends DocumentInserter {

  def addReviewIfOtherDocumentExists(documentIdFieldName: String, documentId: Long, document: JsonObject, userId: Long)
                                    (implicit context: ExecutionContext): Unit = {
    checkIfDocumentExists(documentIdFieldName, documentId, success => {
      if(success){
        addReview(document, userId)
      } else{
        resultHandler(false)
      }
    })
  }

  def addReview(document: JsonObject, userId: Long)(implicit context: ExecutionContext): Unit ={
    mongoClient.insertFuture(collectionToAddTo, getCompleteReview(document, userId))
      .onComplete(onComplete(_, resultHandler))
  }

  def checkIfDocumentExists(documentIdFieldName: String, documentId: Long, resultHandler: Boolean => Unit)
                           (implicit context: ExecutionContext): Unit = {
    mongoClient.count(collectionToSearchIn, Json.obj((documentIdFieldName, documentId)),
      res => resultHandler(res.succeeded() && res.result()>0))
  }
}
