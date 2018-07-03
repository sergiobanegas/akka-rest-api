package infrastructure.repository

import core.persistence.BaseRepository
import core.persistence.error.DBError
import infrastructure.model.dao.Task
import infrastructure.model.dao.table.TaskTable
import slick.lifted.TableQuery
import domain.IdTypes.{TaskId, UserId}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaskRepository extends BaseRepository[TaskTable, Task](TableQuery[TaskTable]) {

  def findUserTasks(userId: UserId): Future[Either[DBError, Seq[Task]]] ={
    this.filter(task => task.userId === userId)
  }

  def findUserTask(id: TaskId, userId: UserId): Future[Either[DBError, Option[Task]]] ={
    this.filterOne(task => task.userId === userId && task.id === id)
  }

  def deleteByUserId(id: UserId): Future[Either[DBError, Future[Seq[Either[DBError, Int]]]]] = {
    this.findUserTasks(id).map({
      case Right(tasks) => Right(Future.sequence(tasks.map(task => this.deleteById(task.id.get))))
      case Left(error) => Left(error)
    })
  }

}
