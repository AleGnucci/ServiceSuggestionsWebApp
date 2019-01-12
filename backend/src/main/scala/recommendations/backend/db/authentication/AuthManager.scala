package recommendations.backend.db.authentication

import io.vertx.scala.ext.auth.{AuthProvider, User}
import io.vertx.scala.ext.web.RoutingContext

trait AuthManager {

  def addUser(userName: String, password: String, resultHandler: Boolean => Unit): Unit

  def login(userName: String, password: String, resultHandler: Option[User] => Unit)(implicit context : RoutingContext)

  def getAuthProvider: AuthProvider
}
