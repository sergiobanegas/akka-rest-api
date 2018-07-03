package core.http

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server
import akka.http.scaladsl.server.{Directive1, ExceptionHandler, Route, StandardRoute}
import core.http.security.AuthenticationHandler

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import core.http.error.{ForbiddenErrorResponse, GenericErrorResponse, InternalServerErrorResponse, UnauthorizedErrorResponse}
import core.generic.{ApplicationError, GenericResponse}
import core.http.constants.{ErrorCodes, Messages}
import core.http.model.{StatusCode, SuccessResponse, UserSessionDetails}
import core.http.util.JsonParser
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.language.postfixOps

trait BaseResource extends AuthenticationHandler with SprayJsonSupport with DefaultJsonProtocol {

  implicit val genericExceptionHandler: server.ExceptionHandler = ExceptionHandler {
    case e =>
      e.printStackTrace()
      val internalServerErrorResponse = InternalServerErrorResponse(message = e.getLocalizedMessage)
      sendResponse(internalServerErrorResponse.code, internalServerErrorResponse)
  }

  protected def sendLoginResponse(userDetails: UserSessionDetails): Route = {
    setCookie(HttpCookie(JWT_COOKIE, value = createAuthorizationHeader(userDetails))) {
      send(SuccessResponse(message = Messages.LoginSuccessful))
    }
  }

  protected def deleteToken(): Route = {
    deleteCookie(JWT_COOKIE) {
      send(SuccessResponse(message = Messages.LogoutSuccessful))
    }
  }

  protected def deleteToken(genericResponse: GenericResponse): Route = {
    deleteCookie(JWT_COOKIE) {
      send(genericResponse)
    }
  }

  protected def call(actor: ActorRef, o: Object): Future[Any] = {
    implicit val timeout: Timeout = Timeout(20 seconds)
    actor ? o
  }

  protected def send(response: GenericResponse): StandardRoute = sendResponse(response.code, response)


  protected def send(error: ApplicationError): StandardRoute = {
    sendResponse(error.code, GenericErrorResponse(error.code, error.errorCode, error.message))
  }

  protected def send(ex: Throwable): StandardRoute = {
    val internalServerErrorResponse = InternalServerErrorResponse(message = ex.getLocalizedMessage)
    sendResponse(internalServerErrorResponse.code, internalServerErrorResponse)
  }

  protected def send[U](o: U): StandardRoute = sendResponse(StatusCode.OK, o)

  protected def authenticated: Directive1[Map[String, Any]] = {
    optionalCookie(JWT_COOKIE).flatMap {
      case Some(cookie) => cookie match {
        case jwt if isTokenExpired(jwt.value) => sendTokenExpiredResponse()
        case jwt if isValidToken(jwt.value) =>
          provide(extractClaims(jwt.value).getOrElse(Map.empty[String, Any]))
        case _ => sendInvalidTokenResponse()
      }
      case _ => sendMissingTokenResponse()
    }
  }

  protected def withRole(role: String): Directive1[Map[String, Any]] = {
    optionalCookie(JWT_COOKIE).flatMap {
      case Some(cookie) => cookie match {
        case jwt if isTokenExpired(jwt.value) => sendTokenExpiredResponse()
        case jwt if isValidToken(jwt.value) =>
          if (userHasRole(jwt.value, role))
            provide(extractClaims(jwt.value).getOrElse(Map.empty[String, Any]))
          else
            sendForbiddenErrorResponse()
        case _ => sendInvalidTokenResponse()
      }
      case _ => sendMissingTokenResponse()
    }
  }

  private def sendTokenExpiredResponse() = {
    sendUnauthorizedResponse(ErrorCodes.getMessageData(ErrorCodes.ExpiredToken))
  }

  private def sendInvalidTokenResponse() = {
    sendUnauthorizedResponse(ErrorCodes.getMessageData(ErrorCodes.InvalidToken))
  }

  private def sendMissingTokenResponse() = {
    sendUnauthorizedResponse(ErrorCodes.getMessageData(ErrorCodes.MissingToken))
  }

  private def sendUnauthorizedResponse(error: (String, String)) = {
    val res = UnauthorizedErrorResponse(errorCode = error._1, message = error._2)
    sendResponse(res.code, res)
  }

  private def sendForbiddenErrorResponse() = {
    val res = ForbiddenErrorResponse()
    sendResponse(res.code, res)
  }

  private def sendResponse[U](statusCode: Int, o: U): StandardRoute = {
    complete(statusCode -> HttpEntity(`application/json`, JsonParser.toJson(o)))
  }
}
