package recommendations.backend.recommendation

import java.util
import java.util.Date

import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.mongo.MongoClient
import org.lenskit.{LenskitBinding, LenskitConfiguration, LenskitRecommender}
import org.lenskit.api.{ItemRecommender, ItemScorer}
import org.lenskit.baseline.{BaselineScorer, ItemMeanRatingItemScorer, UserMeanBaseline, UserMeanItemScorer}
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.dao.DataAccessObject
import org.lenskit.data.ratings.{Rating, RatingBuilder}
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.user.{NeighborFinder, SnapshotNeighborFinder, UserUserItemScorer}
import org.lenskit.transform.normalize.{MeanCenteringVectorNormalizer, UserVectorNormalizer, VectorNormalizer}

import scala.collection.{JavaConverters, mutable}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._
import recommendations.backend.common.names.CollectionNames._
import recommendations.backend.common.names.{ServiceFields, ServiceReviewFields}
import recommendations.backend.common.ServiceCategory

class DefaultRecommendationManager(mongoClient: MongoClient) extends RecommendationManager {

  private val defaultConfig = createRecommenderConfig()

  private def createRecommenderConfig(): LenskitConfiguration ={
    val config = new LenskitConfiguration()
    /*
    // Slope One
    config bind classOf[ItemScorer] to classOf[WeightedSlopeOneItemScorer]
    config bind(classOf[BaselineScorer], classOf[ItemScorer]) to classOf[UserMeanItemScorer]
    config bind (classOf[UserMeanBaseline], classOf[ItemScorer]) to classOf[ItemMeanRatingItemScorer]
    config.set(classOf[DeviationDamping]).asInstanceOf[LenskitBinding[Int]].to(5)
    */
    // Use user-user CF to score items
    config.bind(classOf[ItemScorer]).to(classOf[UserUserItemScorer])
    config.bind(classOf[BaselineScorer], classOf[ItemScorer]).to(classOf[UserMeanItemScorer])
    config.bind(classOf[UserMeanBaseline], classOf[ItemScorer]).to(classOf[ItemMeanRatingItemScorer])
    config.within(classOf[UserVectorNormalizer]).bind(classOf[VectorNormalizer])
      .to(classOf[MeanCenteringVectorNormalizer])
    config.bind(classOf[NeighborFinder]).to(classOf[SnapshotNeighborFinder])
    config.set(classOf[NeighborhoodSize]).asInstanceOf[LenskitBinding[Int]].to(5)
    config
  }

  private def setRecommenderData(data : util.Collection[Rating]) : ItemRecommender = {
    val dataAccessor = StaticDataSource.fromList(data).get()
    val config = defaultConfig.copy()
    config.bind(classOf[DataAccessObject]).to(dataAccessor)
    LenskitRecommender.build(config, dataAccessor).getItemRecommender
  }

  override def recommendServices(userId: Long, serviceCategory: ServiceCategory.Value, resHandler : List[Long] => Unit)
                                (implicit ctx : ExecutionContext): Unit = {
    val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
    mongoClient.findFuture(SERVICES, Json.obj((ServiceFields.CATEGORY, serviceCategory.toString))).onComplete{
      case Success(services) => findRecommendedServices(userId, res => resHandler(res),
        services.filter(service => !service.containsKey(ServiceFields.END_DATE_TIME) ||
          dateFormat.parse(service.getString(ServiceFields.END_DATE_TIME)).after(new Date())).toList)
      case Failure(_) => resHandler(List.empty)
    }
  }


  private def findRecommendedServices(userId: Long, resHandler : List[Long] => Unit, services: List[JsonObject])
                                     (implicit ctx : ExecutionContext):Unit = {
    val serviceIds = services.map(service => service.getLong(ServiceFields.ID))
    val jsonServiceIds = Json.obj(("$in", serviceIds))
    mongoClient.findFuture(SERVICE_REVIEWS, Json.obj((ServiceReviewFields.SERVICE_ID, jsonServiceIds))).onComplete {
      case Success(values) => resHandler(getScalaList(setRecommenderData(values).recommend(userId, 5)))
      case Failure(_) => resHandler(List.empty)
    }
  }

  private implicit def bufferToEvents(buffer: mutable.Buffer[JsonObject]): util.Collection[Rating] = {
    JavaConverters.asJavaCollection(buffer.map(jsonObj => new RatingBuilder()
      .setItemId(jsonObj.getLong(ServiceReviewFields.SERVICE_ID))
      .setRating(jsonObj.getLong(ServiceReviewFields.STARS).toDouble)
      .setTimestamp(jsonObj.getLong(ServiceReviewFields.DATE))
      .setUserId(jsonObj.getLong(ServiceReviewFields.USER_ID)).build()).toList)
  }

  private def getScalaList(list: util.List[java.lang.Long]): List[Long] = {
    list.asScala.toList.map(long => long.toLong)
  }
}