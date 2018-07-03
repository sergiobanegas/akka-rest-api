package infrastructure.service

import akka.actor.Props
import akka.pattern.pipe
import core.service.Service
import core.util.CryptService
import domain.model.{AccountNewData, AccountNewEmailData, AccountNewPasswordData, UserDetails}
import infrastructure.{ErrorCodes, Messages}
import infrastructure.repository.UserRepository
import infrastructure.model.dao.User
import infrastructure.model.error.{BadRequestError, InternalServerError, NotFoundError}
import infrastructure.model.response.OKResponse
import infrastructure.service.AccountRegistryService.{DeleteAccount, GetAccount, UpdateAccount, UpdateEmail, UpdatePassword}
import domain.IdTypes.UserId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountRegistryService {

  final case class GetAccount(id: UserId)

  final case class UpdateAccount(id: UserId, data: AccountNewData)

  final case class UpdatePassword(id: UserId, data: AccountNewPasswordData)

  final case class UpdateEmail(id: UserId, data: AccountNewEmailData)

  final case class DeleteAccount(id: UserId)

  def props: Props = Props[AccountService]
}

class AccountService extends Service {

  val userRepository = new UserRepository

  override def receive: Receive = {
    case GetAccount(id) =>
      userRepository.findOne(id).map({
        case Right(user) => user match {
          case Some(u) => Right(buildUserDetails(u))
          case None => Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.UserNotExists)))
        }
        case Left(error) => Left(new InternalServerError(error))
      }) pipeTo sender
    case UpdatePassword(id, data) =>
      userRepository.findOne(id).flatMap({
        case Right(user) => user match {
          case Some(u) =>
            if (CryptService.isValid(data.oldPassword, u.password)) {
              userRepository.updatePassword(id, CryptService.encrypt(data.password)).map({
                case Right(updatedUser) => Right(buildUserDetails(updatedUser))
                case Left(error) => Left(new InternalServerError(error))
              })
            } else {
              Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.WrongOldPassword))))
            }
          case None => Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.UserNotExists))))
        }
        case Left(error) => Future.successful(Left(new InternalServerError(error)))
      }) pipeTo sender
    case UpdateEmail(id, data) =>
      userRepository.findOne(id).flatMap({
        case Right(user) => user match {
          case Some(u) =>
            if (CryptService.isValid(data.password, u.password)) {
              userRepository.findOneByEmail(data.newEmail).flatMap({
                case Right(optionalUser) =>
                  optionalUser match {
                    case Some(_) => Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.EmailAlreadyExists, Array(data.newEmail)))))
                    case None =>
                      userRepository.updateEmail(id, data.newEmail).map({
                        case Right(updatedUser) => Right(buildUserDetails(updatedUser))
                        case Left(error) => Left(new InternalServerError(error))
                      })
                  }
                case Left(error) => Future.successful(Left(new InternalServerError(error)))
              })
            } else {
              Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.WrongPassword))))
            }
          case None => Future.successful(Left(new NotFoundError(ErrorCodes.getMessageData(ErrorCodes.UserNotExists))))
        }
        case Left(error) => Future.successful(Left(new InternalServerError(error)))
      }) pipeTo sender
    case UpdateAccount(id, data) =>
      userRepository.findOne(id).flatMap {
        case Right(user) => user match {
          case Some(u) =>
            val userToUpdate = u.copy(
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
      } pipeTo sender
    case DeleteAccount(id) =>
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

  private def buildUserDetails(user: User): UserDetails = {
    UserDetails(user.id, user.email, user.userName, user.age, user.gender, user.createdAt, user.updatedAt)
  }

}
