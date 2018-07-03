package infrastructure.service

import akka.actor.Props
import infrastructure.service.UserRegistryService.{DeleteUser, GetUser, GetUsers, UpdateUser}
import akka.pattern.pipe
import core.generic.ApplicationError
import core.service.Service
import core.util.CryptService
import domain.model.{UserDetails, UserNewData}
import infrastructure.{ErrorCodes, Messages}
import infrastructure.repository.UserRepository
import infrastructure.model.dao.User
import infrastructure.model.error.{BadRequestError, InternalServerError, NotFoundError}
import infrastructure.model.response.OKResponse
import domain.IdTypes.UserId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserRegistryService {

  final case object GetUsers

  final case class GetUser(id: UserId)

  final case class UpdateUser(id: UserId, data: UserNewData)

  final case class DeleteUser(id: UserId)

  def props: Props = Props[UserService]
}

class UserService extends Service {

  val userRepository = new UserRepository

  override def receive: Receive = {
    case GetUsers =>
      userRepository.findAll.map({
        case Right(users) => Right(users.map(user => buildUserDetails(user)))
        case Left(error) => Left(new InternalServerError(error))
      }) pipeTo sender
    case GetUser(id) =>
      userRepository.findOne(id).map({
        case Right(user) => user match {
          case Some(u) => Right(buildUserDetails(u))
          case None => Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.UserNotExists)))
        }
        case Left(error) => Left(new InternalServerError(error))
      }) pipeTo sender
    case UpdateUser(id, data) =>
      if (data.email.isDefined) {
        userRepository.findOneByEmail(data.email.get).flatMap {
          case Right(userQuery) => userQuery match {
            case Some(_) => Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.EmailAlreadyExists, Array(data.email.get)))))
            case None => updateUser(id, data)
          }
          case Left(error) => Future.successful(Left(new InternalServerError(error)))
        } pipeTo sender
      } else {
        updateUser(id, data) pipeTo sender
      }
    case DeleteUser(id) =>
      userRepository.findOne(id).flatMap {
        case Right(user) => user match {
          case Some(_) =>
            userRepository.delete(id).map({
              case Right(_) => Right(OKResponse(message = Messages.UserDeleted))
              case Left(error) => Left(new InternalServerError(error))
            })
          case None => Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.UserNotExists))))
        }
        case Left(error) => Future.successful(Left(new InternalServerError(error)))
      } pipeTo sender
  }

  private def updateUser(id: UserId, data: UserNewData): Future[Either[ApplicationError, UserDetails]] = {
    userRepository.findOne(id).flatMap {
      case Right(user) => user match {
        case Some(u) =>
          val userToUpdate = u.copy(
            email = data.email.getOrElse(u.email),
            password = if (data.password.isDefined) CryptService.encrypt(data.password.get) else u.password,
            userName = data.userName.getOrElse(u.userName),
            gender = data.gender.getOrElse(u.gender),
            age = data.age.getOrElse(u.age)
          )
          userRepository.updateById(id, userToUpdate).map {
            case Right(_) => Right(buildUserDetails(userToUpdate))
            case Left(error) => Left(new InternalServerError(error))
          }
        case None => Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.UserNotExists))))
      }
      case Left(error) => Future.successful(Left(new InternalServerError(error)))
    }
  }

  private def buildUserDetails(user: User): UserDetails = {
    UserDetails(user.id, user.email, user.userName, user.age, user.gender, user.createdAt, user.updatedAt)
  }

}
