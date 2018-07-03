package infrastructure.model.error

import core.generic.ApplicationError
import core.http.model.StatusCode

case class NotFoundError(code: Int = StatusCode.NotFound, errorCode: String, message: String) extends ApplicationError {
  def this(error: (String, String)) {
    this(errorCode = error._1, message = error._2)
  }
}
