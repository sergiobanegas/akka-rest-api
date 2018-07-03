package infrastructure.repository

import infrastructure.model.dao.User
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import infrastructure.model.dao.table.UserTable
import core.persistence.BaseRepository
import core.persistence.error.DBError
import domain.IdTypes.UserId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserRepository extends BaseRepository[UserTable, User](TableQuery[UserTable]) {

  val userRoleRepository = new UserRoleRepository
  val taskRepository = new TaskRepository

  def updatePassword(id: UserId, password: String): Future[Either[DBError, User]] = {
    this.findOne(id).flatMap {
      case Right(user) => user match {
        case Some(u) =>
          val userToUpdate = u.copy(password = password)
          this.updateById(id, userToUpdate).map {
            case Right(_) => Right(userToUpdate)
            case Left(error) => Left(error)
          }
        case _ => Future.successful(Left(DBError()))
      }
      case Left(error) => Future.successful(Left(error))
    }
  }

  def updateEmail(id: UserId, email: String): Future[Either[DBError, User]] = {
    this.findOne(id).flatMap {
      case Right(user) => user match {
        case Some(u) =>
          val userToUpdate = u.copy(email = email)
          this.updateById(id, userToUpdate).map {
            case Right(_) => Right(userToUpdate)
            case Left(error) => Left(error)
          }
        case _ => Future.successful(Left(DBError()))
      }
      case Left(error) => Future.successful(Left(error))
    }
  }

  def findOneByEmail(email: String): Future[Either[DBError, Option[User]]] = {
    this.filterOne(user => user.email === email)
  }

  def findOneByEmailAndPassword(email: String, password: String): Future[Either[DBError, Option[User]]] = {
    this.filterOne(user => user.email === email && user.password === password)
  }

  def delete(id: UserId): Future[Either[DBError, Int]] = {
    this.deleteById(id).flatMap({
      case Right(response) =>
        this.taskRepository.deleteByUserId(id).map({
          case Right(_) => Right(response)
          case Left(error) => Left(error)
        })
      case Left(error) => Future.successful(Left(error))
    })
  }

}
