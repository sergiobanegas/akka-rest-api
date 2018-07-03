package infrastructure.model.error

import core.generic.ApplicationError
import core.http.model.StatusCode
import core.persistence.error.DBError
import infrastructure.ErrorCodes

case class InternalServerError(code: Int = StatusCode.InternalServerError, errorCode: String = ErrorCodes.getMessageData(ErrorCodes.InternalServerError)._1, message: String = ErrorCodes.getMessageData(ErrorCodes.InternalServerError)._2, override val exception: Exception = null) extends ApplicationError {

  def this(error: DBError) {
    this(code = StatusCode.InternalServerError, exception = error.exception)
  }
}
