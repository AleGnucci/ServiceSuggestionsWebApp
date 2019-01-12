package recommendations.backend.recommendation

import recommendations.backend.common.ServiceCategory

import scala.concurrent.ExecutionContext

trait RecommendationManager {

  def recommendServices(userId: Long, serviceCategory: ServiceCategory.Value, resultHandler : List[Long] => Unit)
                       (implicit ctx : ExecutionContext)

}
