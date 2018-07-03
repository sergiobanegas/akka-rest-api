package client.resource

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import client.model.request.{AccountPatchRequest, ChangeEmailRequest, ChangePasswordRequest}
import core.http.BaseResource
import client.model.response.UserResponse
import core.generic.{ApplicationError, GenericResponse}
import domain.model.{AccountNewData, AccountNewEmailData, AccountNewPasswordData, UserDetails}
import infrastructure.service.AccountRegistryService.{DeleteAccount, GetAccount, UpdateAccount, UpdateEmail, UpdatePassword}
import spray.json.RootJsonFormat

import scala.language.postfixOps
import scala.util.{Failure, Success}

trait AccountResource extends BaseResource {

  val accountService: ActorRef

  implicit val changePasswordFormat: RootJsonFormat[ChangePasswordRequest] = jsonFormat2(ChangePasswordRequest)
  implicit val changeEmailFormat: RootJsonFormat[ChangeEmailRequest] = jsonFormat2(ChangeEmailRequest)
  implicit val userPatchFormat: RootJsonFormat[AccountPatchRequest] = jsonFormat3(AccountPatchRequest)

  val accountRoutes: Route =
    pathPrefix("account") {
      pathEndOrSingleSlash {
        get {
          authenticated { userInfo =>
            val userResponse = call(accountService, GetAccount(getUserId(userInfo)))
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
            authenticated { userInfo =>
              val deleteUserResponse = call(accountService, DeleteAccount(getUserId(userInfo)))
              onComplete(deleteUserResponse.mapTo[Either[ApplicationError, GenericResponse]]) {
                case Success(response) => response match {
                  case Right(res) => deleteToken(res)
                  case Left(error) => send(error)
                }
                case Failure(ex) => send(ex)
              }
            }
          } ~
          patch {
            authenticated { userInfo =>
              entity(as[AccountPatchRequest]) { userPatchRequest: AccountPatchRequest =>
                val updateResponse = call(accountService, UpdateAccount(getUserId(userInfo), AccountNewData(userPatchRequest.userName, userPatchRequest.age, userPatchRequest.gender)))
                onComplete(updateResponse.mapTo[Either[ApplicationError, UserDetails]]) {
                  case Success(response) => response match {
                    case Right(user) => send(new UserResponse(user))
                    case Left(error) => send(error)
                  }
                  case Failure(ex) => send(ex)
                }
              }
            }
          }
      } ~
        path("password") {
          put {
            authenticated { userInfo =>
              entity(as[ChangePasswordRequest]) { changePasswordRequest =>
                val updatePasswordResponse = call(accountService, UpdatePassword(getUserId(userInfo), AccountNewPasswordData(changePasswordRequest.password, changePasswordRequest.oldPassword)))
                onComplete(updatePasswordResponse.mapTo[Either[ApplicationError, UserDetails]]) {
                  case Success(response) => response match {
                    case Right(user) => send(new UserResponse(user))
                    case Left(error) => send(error)
                  }
                  case Failure(ex) => send(ex)
                }
              }
            }
          }
        } ~
        path("email") {
          put {
            authenticated { userInfo =>
              entity(as[ChangeEmailRequest]) { changeEmailRequest: ChangeEmailRequest =>
                val updatePasswordResponse = call(accountService, UpdateEmail(getUserId(userInfo), AccountNewEmailData(changeEmailRequest.email, changeEmailRequest.password)))
                onComplete(updatePasswordResponse.mapTo[Either[ApplicationError, UserDetails]]) {
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
