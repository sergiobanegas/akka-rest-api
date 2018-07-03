package client.resource

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import client.model.request.{CreateTaskRequest, TaskPatchRequest}
import client.model.response.TaskResponse
import core.generic.{ApplicationError, GenericResponse}
import core.http.BaseResource
import domain.model.{NewTaskDetails, TaskDetails, TaskNewData}
import infrastructure.service.TaskRegistryService.{CreateTask, DeleteTask, GetTask, GetTasks, UpdateTask}
import spray.json.RootJsonFormat

import scala.language.postfixOps
import scala.util.{Failure, Success}

trait TaskResource extends BaseResource {

  import client.marshaller.DateMarshaller._

  implicit val createTaskFormat: RootJsonFormat[CreateTaskRequest] = jsonFormat4(CreateTaskRequest)
  implicit val updateTaskFormat: RootJsonFormat[TaskPatchRequest] = jsonFormat4(TaskPatchRequest)

  val taskService: ActorRef

  val taskRoutes: Route =
    pathPrefix("tasks") {
      pathEndOrSingleSlash {
        post {
          authenticated { userInfo =>
            entity(as[CreateTaskRequest]) { createTaskRequest =>
              val createTaskResponse = call(taskService, CreateTask(getUserId(userInfo), NewTaskDetails(createTaskRequest.title, createTaskRequest.content, createTaskRequest.effectiveDate, createTaskRequest.expirationDate)))
              onComplete(createTaskResponse.mapTo[Either[ApplicationError, TaskDetails]]) {
                case Success(response) => response match {
                  case Right(task) => send(new TaskResponse(task))
                  case Left(error) => send(error)
                }
                case Failure(ex) => send(ex)
              }
            }
          }
        } ~
          get {
            authenticated { userInfo =>
              val tasksResponse = call(taskService, GetTasks(getUserId(userInfo)))
              onComplete(tasksResponse.mapTo[Either[ApplicationError, Seq[TaskDetails]]]) {
                case Success(response) => response match {
                  case Right(tasks) => send(tasks.map(t => new TaskResponse(t)).toArray)
                  case Left(error) => send(error)
                }
                case Failure(ex) => send(ex)
              }
            }
          }
      } ~
        path(LongNumber) { number =>
          get {
            authenticated { userInfo =>
              val taskResponse = call(taskService, GetTask(number, getUserId(userInfo)))
              onComplete(taskResponse.mapTo[Either[ApplicationError, TaskDetails]]) {
                case Success(response) => response match {
                  case Right(task) => send(new TaskResponse(task))
                  case Left(error) => send(error)
                }
                case Failure(ex) => send(ex)
              }
            }
          } ~
            patch {
              authenticated { userInfo =>
                entity(as[TaskPatchRequest]) { updateTaskRequest =>
                  val updateTaskResponse = call(taskService, UpdateTask(number, getUserId(userInfo), TaskNewData(updateTaskRequest.title, updateTaskRequest.content, updateTaskRequest.effectiveDate, updateTaskRequest.expirationDate)))
                  onComplete(updateTaskResponse.mapTo[Either[ApplicationError, TaskDetails]]) {
                    case Success(response) => response match {
                      case Right(task) => send(new TaskResponse(task))
                      case Left(error) => send(error)
                    }
                    case Failure(ex) => send(ex)
                  }
                }
              }
            } ~
            delete {
              authenticated { userInfo =>
                val deleteTaskResponse = call(taskService, DeleteTask(number, getUserId(userInfo)))
                onComplete(deleteTaskResponse.mapTo[Either[ApplicationError, GenericResponse]]) {
                  case Success(response) => response match {
                    case Right(deleteTaskPayload) => send(deleteTaskPayload)
                    case Left(error) => send(error)
                  }
                  case Failure(ex) => send(ex)
                }
              }
            }
        }
    }
}
