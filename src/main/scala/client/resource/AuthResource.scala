package client.resource

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import infrastructure.service.AuthRegistryService.{Login, SignUp}
import client.model.request.{LoginRequest, SignUpRequest}
import core.http.BaseResource
import core.generic.{ApplicationError, GenericResponse}
import core.http.model.UserSessionDetails
import domain.model.SignUpDetails
import spray.json.RootJsonFormat

import scala.language.postfixOps
import scala.util.{Failure, Success}

trait AuthResource extends BaseResource {

  val authService: ActorRef

  implicit val signUpFormat: RootJsonFormat[SignUpRequest] = jsonFormat5(SignUpRequest)
  implicit val loginFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)

  val authRoutes: Route =
    pathPrefix("login") {
      post {
        entity(as[LoginRequest]) { loginRequest =>
          val loginResponse = call(authService, Login(loginRequest.email, loginRequest.password))
          onComplete(loginResponse.mapTo[Either[ApplicationError, UserSessionDetails]]) {
            case Success(response) =>
              response match {
                case Right(userDetails) => sendLoginResponse(userDetails)
                case Left(error) => send(error)
              }
            case Failure(ex) => send(ex)
          }
        }
      }
    } ~
      pathPrefix("sign-up") {
        post {
          entity(as[SignUpRequest]) { signUpRequest =>
            val signUpResponse = call(authService, SignUp(SignUpDetails(signUpRequest.email, signUpRequest.userName, signUpRequest.password, signUpRequest.age, signUpRequest.gender)))
            onComplete(signUpResponse.mapTo[Either[ApplicationError, GenericResponse]]) {
              case Success(response) => response match {
                case Right(signUpPayload) => send(signUpPayload)
                case Left(error) => send(error)
              }
              case Failure(ex) => send(ex)
            }
          }
        }
      } ~
        pathPrefix("logout") {
          post {
            deleteToken()
          }
        }

}
