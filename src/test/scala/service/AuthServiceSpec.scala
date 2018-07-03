package service

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import core.generic.{ApplicationError, GenericResponse}
import core.http.model.{StatusCode, UserSessionDetails}
import domain.model.SignUpDetails
import infrastructure.service.AuthRegistryService
import infrastructure.service.AuthRegistryService.{Login, SignUp}
import infrastructure.ErrorCodes
import org.scalatest.{FlatSpecLike, MustMatchers}
import util.BaseTest

class AuthServiceSpec extends TestKit(ActorSystem()) with ImplicitSender with FlatSpecLike with MustMatchers with BaseTest {

  val sender = TestProbe()
  val authService: ActorRef = system.actorOf(AuthRegistryService.props)

  "Auth service" should "return user details when logging in" in {
    sender.send(authService, Login("admin@gmail.com", "1234"))
    val response = sender.expectMsgType[Either[ApplicationError, UserSessionDetails]]
    response.isRight must be(true)
    response.right.get must equal(UserSessionDetails(Some(1), "ADMIN"))
  }

  "Auth service" should "return an error when incorrect data logging in" in {
    sender.send(authService, Login("admin2@gmail.com", "1234"))
    val response = sender.expectMsgType[Either[ApplicationError, UserSessionDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.WrongCredentials)
  }

  "Auth service" should "return an error when signing up with an existing user" in {
    sender.send(authService, SignUp(SignUpDetails("admin@gmail.com", "1234", "Sergio", 25, "F")))
    val response = sender.expectMsgType[Either[ApplicationError, GenericResponse]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.EmailAlreadyExists)
  }

  "Auth service" should "create an user" in {
    sender.send(authService, SignUp(SignUpDetails("admin3@gmail.com", "1234", "Sergio", 25, "M")))
    val response = sender.expectMsgType[Either[ApplicationError, GenericResponse]]
    response.isRight must be(true)
    response.right.get.code must equal(StatusCode.Created)
  }

}
