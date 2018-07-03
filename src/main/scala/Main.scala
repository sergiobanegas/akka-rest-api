import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import client.Routes
import infrastructure.service.{AccountRegistryService, AuthRegistryService, TaskRegistryService, UserRegistryService}
import config.{Config, MigrationConfig}

import scala.concurrent.ExecutionContext

object Main extends App with Config with MigrationConfig with Routes {
  private implicit val system: ActorSystem = ActorSystem()
  protected implicit val executor: ExecutionContext = system.dispatcher
  protected val log: LoggingAdapter = Logging(system, getClass)
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  val userService = system.actorOf(UserRegistryService.props)
  val authService = system.actorOf(AuthRegistryService.props)
  val accountService = system.actorOf(AccountRegistryService.props)
  val taskService = system.actorOf(TaskRegistryService.props)

  migrate()
  Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)
}
