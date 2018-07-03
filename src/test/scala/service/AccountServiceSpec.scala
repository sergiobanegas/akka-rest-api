package service

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import core.generic.ApplicationError
import core.http.model.StatusCode
import domain.model.{AccountNewData, AccountNewEmailData, AccountNewPasswordData, UserDetails}
import infrastructure.ErrorCodes
import infrastructure.model.response.OKResponse
import infrastructure.service.AccountRegistryService
import infrastructure.service.AccountRegistryService.{DeleteAccount, GetAccount, UpdateAccount, UpdateEmail, UpdatePassword}
import org.scalatest.{FlatSpecLike, MustMatchers}
import util.BaseTest

class AccountServiceSpec extends TestKit(ActorSystem()) with ImplicitSender with FlatSpecLike with MustMatchers with BaseTest {

  val sender = TestProbe()
  val accountService: ActorRef = system.actorOf(AccountRegistryService.props)

  "Account service" should "return an account" in {
    sender.send(accountService, GetAccount(1))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isRight must be(true)
    response.right.get.email must equal("admin@gmail.com")
  }

  "Account service" should "throw an error when returning an account that doesn't exists" in {
    sender.send(accountService, GetAccount(2))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

  "Account service" should "update an account" in {
    sender.send(accountService, UpdateAccount(1, AccountNewData(Some("New name"), None, None)))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isRight must be(true)
    response.right.get.userName must equal("New name")
  }

  "Account service" should "throw an error when updating an account that doesn't exists" in {
    sender.send(accountService, UpdateAccount(2, AccountNewData(Some("New name"), None, None)))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

  "Account service" should "throw an error when updating the password with incorrect old password" in {
    sender.send(accountService, UpdatePassword(1, AccountNewPasswordData("4321", "1111")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.WrongOldPassword)
  }

  "Account service" should "update the password of an account" in {
    sender.send(accountService, UpdatePassword(1, AccountNewPasswordData("4321", "1234")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isRight must be(true)
    response.right.get.email must equal("admin@gmail.com")
  }

  "Account service" should "throw an error when updating the password of a non existing account" in {
    sender.send(accountService, UpdatePassword(10, AccountNewPasswordData("4321", "1234")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

  "Account service" should "throw an error when updating the email with incorrect password" in {
    sender.send(accountService, UpdateEmail(1, AccountNewEmailData("adminnewmail@gmail.com", "1111")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.WrongPassword)
  }

  "Account service" should "throw an error when updating the email with one that already exists" in {
    sender.send(accountService, UpdateEmail(1, AccountNewEmailData("admin@gmail.com", "4321")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.EmailAlreadyExists)
  }

  "Account service" should "update the email of an account" in {
    sender.send(accountService, UpdateEmail(1, AccountNewEmailData("adminnewmail@gmail.com", "4321")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isRight must be(true)
    response.right.get.email must equal("adminnewmail@gmail.com")
  }

  "Account service" should "throw an error when updating the email of a non existing account" in {
    sender.send(accountService, UpdateEmail(100, AccountNewEmailData("newemail@gmail.com", "1234")))
    val response = sender.expectMsgType[Either[ApplicationError, UserDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

  "Account service" should "remove an account" in {
    sender.send(accountService, DeleteAccount(1))
    val response = sender.expectMsgType[Either[ApplicationError, OKResponse]]
    response.isRight must be(true)
    response.right.get.code must equal(StatusCode.OK)
  }

  "Account service" should "throw an error when deleting an account that doesn't exists" in {
    sender.send(accountService, DeleteAccount(2))
    val response = sender.expectMsgType[Either[ApplicationError, OKResponse]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.UserNotExists)
  }

}
