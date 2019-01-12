package recommendations.backend.db.util

import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import io.vertx.scala.ext.mongo.MongoClient
import recommendations.backend.db.DatabaseManagerHelper.onGetComplete

import scala.concurrent.ExecutionContext

class DefaultDocumentSearcher(mongoClient: MongoClient, resultHandler: Option[JsonArray] => Unit)
  extends DocumentSearcher {

  override def search(collection: String, fieldName: String, fieldValue: String)
                     (implicit context: ExecutionContext): Unit =
    mongoClient.findFuture(collection, getSearchQuery(fieldName, fieldValue))
      .onComplete(onGetComplete(_, resultHandler))

  private def getSearchQuery(fieldName: String, fieldValue: String): JsonObject = {
    val regex = fieldValue
      .split(" ")
      .map(word => "(?=.*\\b(" + getCapitalizedWord(word) + "|" + word.capitalize + ")\\b)")
      .mkString("") + ".*"
    Json.obj((fieldName, Json.obj(("$regex", regex))))
  }

  private def getCapitalizedWord(word: String): String = {
    Character.toLowerCase(word.charAt(0))+word.substring(1)
  }
}
