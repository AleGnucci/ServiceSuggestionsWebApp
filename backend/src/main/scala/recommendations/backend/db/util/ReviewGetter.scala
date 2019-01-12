package recommendations.backend.db.util

import scala.concurrent.ExecutionContext

trait ReviewGetter {
  def getReviews(collection: String, fieldName: String, fieldValue: Long)
                (implicit context: ExecutionContext): Unit
}
