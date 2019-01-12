package recommendations.backend.db.authentication

import io.vertx.ext.auth.mongo.HashSaltStyle
import io.vertx.scala.ext.web.RoutingContext
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.auth.{AuthProvider, User}
import io.vertx.scala.ext.auth.mongo.MongoAuth
import io.vertx.scala.ext.mongo.MongoClient
import recommendations.backend.common.names.CollectionNames._
import recommendations.backend.common.names.UserFields

import scala.collection.mutable

class DefaultAuthManager(mongoClient : MongoClient, vertx: Vertx) extends AuthManager {

  private val authProvider = setupAuthProvider()

  private def setupAuthProvider() : MongoAuth = {
    val authProvider = MongoAuth.create(mongoClient, new JsonObject())
      .setCollectionName(USERS)
      .setUsernameField(UserFields.NAME)
      .setPasswordField(UserFields.PASSWORD)
      .setSaltField(UserFields.SALT)
      .setRoleField(UserFields.ROLES)
    authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.COLUMN)
    authProvider
  }

  override def addUser(userName: String, password: String, resultHandler: Boolean => Unit): Unit = {
    authProvider.insertUser(userName, password, mutable.Buffer.empty, mutable.Buffer.empty,
      res => resultHandler(res.succeeded()))
  }

  override def login(userName: String, password: String, resultHandler: Option[User] => Unit)
                    (implicit context: RoutingContext): Unit = {
    authProvider.authenticate(Json.obj(("username", userName), ("password", password)),
      res => if(res.succeeded()) {
        context.setUser(res.result())
        resultHandler(Option(res.result()))
      } else {
        resultHandler(None)
      })
  }

  override def getAuthProvider: AuthProvider = authProvider
}
