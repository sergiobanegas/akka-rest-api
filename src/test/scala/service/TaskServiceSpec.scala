package service

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import core.generic.ApplicationError
import core.http.model.StatusCode
import domain.model.{NewTaskDetails, TaskDetails, TaskNewData}
import infrastructure.ErrorCodes
import infrastructure.model.dao.Task
import infrastructure.model.response.OKResponse
import infrastructure.repository.TaskRepository
import infrastructure.service.TaskRegistryService
import infrastructure.service.TaskRegistryService.{CreateTask, DeleteTask, GetTask, GetTasks, UpdateTask}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, MustMatchers}
import util.BaseTest

import scala.concurrent.ExecutionContext.Implicits.global

class TaskServiceSpec extends TestKit(ActorSystem()) with ImplicitSender with FlatSpecLike with MustMatchers with BaseTest with BeforeAndAfterAll {

  val taskRepository = new TaskRepository
  val sender = TestProbe()

  val taskService: ActorRef = system.actorOf(TaskRegistryService.props)

  override def beforeAll {
    for {
      _ <- taskRepository.save(Task(userId = 1, title = "Test 1", content = "Content", effectiveDate = new Date))
      _ <- taskRepository.save(Task(userId = 1, title = "Test 2", content = "Content", effectiveDate = new Date))
    } yield null
  }

  "Task service" should "return list of tasks" in {
    sender.send(taskService, GetTasks(1))
    val response = sender.expectMsgType[Either[ApplicationError, Seq[TaskDetails]]]
    response.isRight must be(true)
    response.right.get.size must equal(2)
  }

  "Task service" should "return a task" in {
    sender.send(taskService, GetTask(1, 1))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isRight must be(true)
    response.right.get.title must equal("Test 1")
  }

  "Task service" should "throw an error when returning a non existing task" in {
    sender.send(taskService, GetTask(1, 10))
    val response = sender.expectMsgType[Either[ApplicationError, Seq[TaskDetails]]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.TaskNotExists)
  }

  "Task service" should "create a task" in {
    sender.send(taskService, CreateTask(1, NewTaskDetails("Title test", "content", new Date, None)))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isRight must be(true)
    response.right.get.title must equal("Title test")
  }

  "Task service" should "throw an error when creating a task with same dates" in {
    val date = new Date
    sender.send(taskService, CreateTask(1, NewTaskDetails("Title test", "content", date, Some(date))))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.ExpirationDateBeforeOrSameEffectiveDate)
  }

  "Task service" should "throw an error when creating a task with incorrect expiration date" in {
    sender.send(taskService, CreateTask(1, NewTaskDetails("Title test", "content", new Date, Some(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)))))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.ExpirationDateBeforeOrSameEffectiveDate)
  }

  "Task service" should "update a task" in {
    sender.send(taskService, UpdateTask(1, 1, TaskNewData(Some("New title"), None, None, None)))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isRight must be(true)
    response.right.get.title must equal("New title")
  }

  "Task service" should "throw an error when updating a non existing task" in {
    sender.send(taskService, UpdateTask(10, 1, TaskNewData(Some("New title"), None, None, None)))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.TaskNotExists)
  }

  "Task service" should "delete a task" in {
    sender.send(taskService, DeleteTask(1, 1))
    val response = sender.expectMsgType[Either[ApplicationError, OKResponse]]
    response.isRight must be(true)
    response.right.get.code must equal(StatusCode.OK)
  }

  "Task service" should "throw an error when deleting a non existing task" in {
    sender.send(taskService, DeleteTask(10, 1))
    val response = sender.expectMsgType[Either[ApplicationError, TaskDetails]]
    response.isLeft must be(true)
    response.left.get.errorCode must equal(ErrorCodes.TaskNotExists)
  }

}
