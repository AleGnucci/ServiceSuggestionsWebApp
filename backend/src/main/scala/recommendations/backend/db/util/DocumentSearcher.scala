package recommendations.backend.db.util

import scala.concurrent.ExecutionContext

trait DocumentSearcher {
  def search(collection: String, fieldName: String, fieldValue: String)
            (implicit context: ExecutionContext): Unit
}
