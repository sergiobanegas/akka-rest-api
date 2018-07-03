package service

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import core.generic.ApplicationError
import core.http.model.StatusCode
import domain.model.{UserDetails, UserNewData}
import infrastructure.ErrorCodes
import infrastructure.model.response.OKResponse
import infrastructure.service.UserRegistryService
import infrastructure.service.UserRegistryService.{DeleteUser, GetUser, GetUsers, UpdateUser}
import org.scalatest.{FlatSpecLike, MustMatchers}
import util.BaseTest

class UserServiceSpec extends TestKit(ActorSystem()) with ImplicitSender with FlatSpecLike with MustMatchers with BaseTest {

  val sender = TestProbe()
  val userService: ActorRef = system.actorOf(UserRegistryService.props)

  "User service" should "return list of users" in {
    sender.send(userService, GetUsers)
    val response = sender.expectMsgType[Either[ApplicationError, Seq[UserDetails]]]
    response.isRight must be(true)
    response.right.get.size must equal(1)
    response.right.get.head.email must equal("admin@gmail.com")
  }

  "User service" should "return a specific user" in {
    sender.send(userService, GetUser(1))
    val state = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    state.isRight must be(true)
    state.right.get.email must equal("admin@gmail.com")
  }

  "User service" should "throw an error when returning an user that doesn't exists" in {
    sender.send(userService, GetUser(2))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

  "User service" should "update an user" in {
    sender.send(userService, UpdateUser(1, UserNewData(None, None, Some("New name"), None, None)))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isRight must be(true)
    response.right.get.userName must equal("New name")
  }

  "User service" should "throw an error when updating an user that doesn't exists" in {
    sender.send(userService, UpdateUser(2, UserNewData(None, None, Some("New name"), None, None)))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

  "User service" should "remove an user" in {
    sender.send(userService, DeleteUser(1))
    val response = sender.expectMsgType[Either[ApplicationError, OKResponse]]
    response.isRight must be(true)
    response.right.get.code must equal(StatusCode.OK)
  }

  "User service" should "throw an error when deleting an user that doesn't exists" in {
    sender.send(userService, DeleteUser(2))
    val response = sender.expectMsgType[Either[ApplicationError, OKResponse]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

}
