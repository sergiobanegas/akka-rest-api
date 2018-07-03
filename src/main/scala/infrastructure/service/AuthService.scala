package infrastructure.service

import akka.actor.Props
import akka.pattern.pipe
import core.generic.{ApplicationError, GenericResponse}
import core.http.model.UserSessionDetails
import core.persistence.error.DBError
import core.service.Service
import core.util.CryptService
import infrastructure.repository.{UserRepository, UserRoleRepository}
import infrastructure.model.dao.User
import domain.model.SignUpDetails
import infrastructure.{ErrorCodes, Messages}
import infrastructure.model.error.{BadRequestError, InternalServerError}
import infrastructure.model.response.CreatedResponse
import infrastructure.service.AuthRegistryService.{Login, SignUp}
import domain.IdTypes.UserId
import infrastructure.model.dao.enumtypes.Gender

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AuthRegistryService {

  final case class Login(email: String, password: String)

  final case class SignUp(user: SignUpDetails)

  def props: Props = Props[AuthService]
}

class AuthService extends Service {

  val userRepository = new UserRepository
  val userRoleRepository = new UserRoleRepository

  override def receive: Receive = {
    case Login(email, password) =>
      (for {
        user <- userRepository.findOneByEmail(email)
        session <- generateUserDetails(user, password)
      } yield session) pipeTo sender
    case SignUp(user) =>
      if (user.age <= 0 || user.age >= 125) {
        Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.InvalidAge)))) pipeTo sender
      } else if (!Gender.isGenderType(user.gender)) {
        Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.InvalidGender)))) pipeTo sender
      } else {
        (for {
          userQueryResponse <- userRepository.findOneByEmail(user.email)
          response <- createUser(userQueryResponse, user)
        } yield response) pipeTo sender
      }
  }

  private def createUserSessionDetails(id: Option[UserId], role: String): UserSessionDetails = {
    UserSessionDetails(id, role)
  }

  private def generateUserDetails(userResponse: Either[DBError, Option[User]], password: String): Future[Either[ApplicationError, UserSessionDetails]] = {
    userResponse match {
      case Right(optionalUser) => optionalUser match {
        case Some(user) =>
          if (CryptService.isValid(password, user.password)) {
            userRoleRepository.getUserRole(user.id.get).map {
              case Right(role) => Right(createUserSessionDetails(user.id, role.get))
              case Left(error) => Left(new InternalServerError(error))
            }
          } else {
            Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.WrongCredentials))))
          }
        case None => Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.WrongCredentials))))
      }
      case Left(error) => Future.successful(Left(new InternalServerError(error)))
    }
  }

  private def createUser(userQueryResponse: Either[DBError, Option[User]], signUpDetails: SignUpDetails): Future[Either[ApplicationError, GenericResponse]] = {
    userQueryResponse match {
      case Right(userOptional) =>
        userOptional match {
          case Some(_) => Future.successful(Left(new BadRequestError(ErrorCodes.getMessageData(ErrorCodes.EmailAlreadyExists, Array(signUpDetails.email)))))
          case None =>
            for {
              userCreationResponse <- userRepository.save(User(email = signUpDetails.email, userName = signUpDetails.userName, password = CryptService.encrypt(signUpDetails.password), age = signUpDetails.age, gender = signUpDetails.gender))
              response <- createRole(userCreationResponse)
            } yield response
        }
      case Left(error) => Future.successful(Left(new InternalServerError(error)))
    }
  }

  private def createRole(createUserResponse: Either[DBError, User]): Future[Either[InternalServerError, CreatedResponse]] = {
    createUserResponse match {
      case Right(createdUser) =>
        userRoleRepository.save(createdUser.id.get).map {
          case Right(_) => Right(CreatedResponse(message = Messages.UserCreated))
          case Left(error) =>
            userRepository.deleteById(createdUser.id.get)
            Left(new InternalServerError(error))
        }
      case Left(error) => Future.successful(Left(new InternalServerError(error)))
    }
  }
}
