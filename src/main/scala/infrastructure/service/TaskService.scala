package infrastructure.service

import java.util.concurrent.TimeUnit
import java.util.Date

import akka.actor.Props
import akka.pattern.pipe
import core.service.Service
import domain.model.{NewTaskDetails, TaskDetails, TaskNewData}
import infrastructure.{ErrorCodes, Messages}
import infrastructure.repository.TaskRepository
import infrastructure.model.dao.Task
import infrastructure.model.error.{BadRequestError, InternalServerError, NotFoundError}
import infrastructure.model.response.OKResponse
import infrastructure.service.TaskRegistryService.{CreateTask, DeleteTask, GetTask, GetTasks, UpdateTask}
import domain.IdTypes.{TaskId, UserId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaskRegistryService {

  final case class CreateTask(userId: UserId, data: NewTaskDetails)

  final case class GetTasks(userId: UserId)

  final case class GetTask(userId: UserId, id: TaskId)

  final case class UpdateTask(id: TaskId, userId: UserId, data: TaskNewData)

  final case class DeleteTask(userId: UserId, id: TaskId)

  def props: Props = Props[TaskService]
}

class TaskService extends Service {

  val taskRepository = new TaskRepository

  override def receive: Receive = {
    case CreateTask(userId, data) =>
      if (!areValidDates(data.effectiveDate, data.expirationDate)) {
        Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.ExpirationDateBeforeOrSameEffectiveDate)))) pipeTo sender
      } else {
        taskRepository.save(Task(userId = userId, title = data.title, content = data.content, effectiveDate = data.effectiveDate, expirationDate = data.expirationDate)).map({
          case Right(task) => Right(createTaskDetails(task))
          case Left(error) => Left(new InternalServerError(error))
        }) pipeTo sender
      }
    case GetTasks(userId) =>
      taskRepository.findUserTasks(userId).map({
        case Right(tasks) => Right(tasks.map(task => createTaskDetails(task)))
        case Left(error) => Left(new InternalServerError(error))
      }) pipeTo sender
    case GetTask(id, userId) =>
      taskRepository.findUserTask(id, userId).map({
        case Right(user) => user match {
          case Some(u) => Right(createTaskDetails(u))
          case None => Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.TaskNotExists)))
        }
        case Left(error) => Left(new InternalServerError(error))
      }) pipeTo sender
    case DeleteTask(id, userId) =>
      taskRepository.findUserTask(id, userId).flatMap({
        case Right(user) => user match {
          case Some(_) =>
            taskRepository.deleteById(id).map({
              case Right(_) => Right(OKResponse(message = Messages.TaskDeleted))
              case Left(error) => Left(new InternalServerError(error))
            })
          case None => Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.TaskNotExists))))
        }
        case Left(error) => Future.successful(Left(new InternalServerError(error)))
      }) pipeTo sender
    case UpdateTask(id, userId, data) =>
      taskRepository.findUserTask(id, userId).flatMap({
        case Right(task) => task match {
          case Some(t) =>
            if (!areValidDatesToUpdate(t, data.effectiveDate, data.expirationDate)) {
              Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.ExpirationDateBeforeOrSameEffectiveDate))))
            } else {
              val taskToUpdate = t.copy(
                title = data.title.getOrElse(t.title),
                content = data.content.getOrElse(t.content),
                effectiveDate = data.effectiveDate.getOrElse(t.effectiveDate),
                expirationDate = if (data.expirationDate.isDefined) data.expirationDate else t.expirationDate
              )
              taskRepository.updateById(id, taskToUpdate).map {
                case Right(_) => Right(createTaskDetails(taskToUpdate))
                case Left(error) => Left(new InternalServerError(error))
              }
            }
          case None => Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.TaskNotExists))))
        }
        case Left(error) => Future.successful(Left(new InternalServerError(error)))
      }) pipeTo sender
  }

  private def createTaskDetails(task: Task): TaskDetails = {
    TaskDetails(task.id, task.title, task.content, task.effectiveDate, task.expirationDate, task.createdAt, task.updatedAt)
  }

  private def areValidDates(effectiveDate: Date, expirationDate: Option[Date]): Boolean =
    if (expirationDate.isDefined) effectiveDate.compareTo(expirationDate.get) < 0 else true

  private def areValidDatesToUpdate(task: Task, effectiveDate: Option[Date], expirationDate: Option[Date]): Boolean = {
    areEmptyDates(effectiveDate, expirationDate) || existsEffectiveDateAndIsValid(task, effectiveDate, expirationDate) || existsExpirationDateAndIsValid(task, effectiveDate, expirationDate)
  }

  private def existsEffectiveDateAndIsValid(task: Task, effectiveDate: Option[Date], expirationDate: Option[Date]): Boolean = {
    effectiveDate.isDefined && effectiveDate.get.compareTo(expirationDate.getOrElse(task.expirationDate.getOrElse(getDayAfter(effectiveDate.get)))) < 0
  }

  private def existsExpirationDateAndIsValid(task: Task, effectiveDate: Option[Date], expirationDate: Option[Date]): Boolean = {
    expirationDate.isDefined && effectiveDate.getOrElse(task.effectiveDate).compareTo(expirationDate.get) < 0
  }

  private def areEmptyDates(effectiveDate: Option[Date], expirationDate: Option[Date]): Boolean = {
    expirationDate.isEmpty && effectiveDate.isEmpty
  }

  private def getDayAfter(date: Date): Date = new Date(date.getTime + TimeUnit.DAYS.toMillis(1))
  
}
