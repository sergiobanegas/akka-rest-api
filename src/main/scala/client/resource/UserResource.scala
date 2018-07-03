package client.resource

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import client.model.request.UserPatchRequest
import infrastructure.service.UserRegistryService.{DeleteUser, GetUser, GetUsers, UpdateUser}
import client.model.response.UserResponse
import core.http.BaseResource
import core.generic.{ApplicationError, GenericResponse, UserRole}
import domain.model.{UserDetails, UserNewData}
import spray.json.RootJsonFormat

import scala.language.postfixOps
import scala.util.{Failure, Success}

trait UserResource extends BaseResource {

  val userService: ActorRef

  implicit val updateUserFormat: RootJsonFormat[UserPatchRequest] = jsonFormat5(UserPatchRequest)

  val userRoutes: Route =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          withRole(UserRole.ADMIN.toString) { _ =>
            val usersResponse = call(userService, GetUsers)
            onComplete(usersResponse.mapTo[Either[ApplicationError, Seq[UserDetails]]]) {
              case Success(response) => response match {
                case Right(users) => send(users.map(u => new UserResponse(u)).toArray)
                case Left(error) => send(error)
              }
              case Failure(ex) => send(ex)
            }
          }
        }
      } ~
        path(LongNumber) { number =>
          get {
            withRole(UserRole.ADMIN.toString) { _ =>
              val userResponse = call(userService, GetUser(number))
              onComplete(userResponse.mapTo[Either[ApplicationError, UserDetails]]) {
                case Success(response) => response match {
                  case Right(user) => send(new UserResponse(user))
                  case Left(error) => send(error)
                }
                case Failure(ex) => send(ex)
              }
            }
          } ~
            delete {
              withRole(UserRole.ADMIN.toString) { _ =>
                val deleteUserResponse = call(userService, DeleteUser(number))
                onComplete(deleteUserResponse.mapTo[Either[ApplicationError, GenericResponse]]) {
                  case Success(response) => response match {
                    case Right(deleteUserPayload) => send(deleteUserPayload)
                    case Left(error) => send(error)
                  }
                  case Failure(ex) => send(ex)
                }
              }
            } ~
            patch {
              withRole(UserRole.ADMIN.toString) { _ =>
                entity(as[UserPatchRequest]) { userPatchRequest: UserPatchRequest =>
                  val updateUserResponse = call(userService, UpdateUser(number, UserNewData(userPatchRequest.email, userPatchRequest.password, userPatchRequest.userName, userPatchRequest.age, userPatchRequest.gender)))
                  onComplete(updateUserResponse.mapTo[Either[ApplicationError, UserDetails]]) {
                    case Success(response) => response match {
                      case Right(user) => send(new UserResponse(user))
                      case Left(error) => send(error)
                    }
                    case Failure(ex) => send(ex)
                  }
                }
              }
            }
        }
    }
}
