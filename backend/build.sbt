name := "Service_Suggestions_Web_App"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += "Java.net Maven2 Repository" at "https://jitpack.io"

libraryDependencies += "io.vertx" %% "vertx-lang-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-web-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-mongo-client-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-auth-common-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-auth-mongo-scala" % "3.5.3"
libraryDependencies += "org.lenskit" % "lenskit-all" % "3.0-M2"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.2.0"



