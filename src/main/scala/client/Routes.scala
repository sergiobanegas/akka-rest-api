package client

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import client.resource.{AccountResource, AuthResource, TaskResource, UserResource}

trait Routes extends UserResource with AuthResource with AccountResource with TaskResource {
  val routes: Route =
    pathPrefix("v1") {
      authRoutes ~
      userRoutes ~
      accountRoutes ~
      taskRoutes
    } ~ path("")(getFromResource("public/index.html"))
}
