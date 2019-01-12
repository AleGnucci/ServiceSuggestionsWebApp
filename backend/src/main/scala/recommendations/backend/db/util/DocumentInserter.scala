package recommendations.backend.db.util

import io.vertx.lang.scala.json.JsonObject

import scala.concurrent.ExecutionContext

trait DocumentInserter {

  def addReviewIfOtherDocumentExists(otherDocumentIdFieldName: String, otherDocumentId: Long,
                                     documentToInsert: JsonObject, userId: Long)
                                    (implicit context: ExecutionContext)

  def addReview(document: JsonObject, userId: Long)(implicit context: ExecutionContext)

  def checkIfDocumentExists(documentIdFieldName: String, documentId: Long, resultHandler: Boolean => Unit)
                           (implicit context: ExecutionContext)
}
