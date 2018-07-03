package core.http.model

import akka.http.scaladsl.model.StatusCodes

object StatusCode {
  val Forbidden: Int = StatusCodes.Forbidden.intValue
  val InternalServerError: Int = StatusCodes.InternalServerError.intValue
  val Unauthorized: Int = StatusCodes.Unauthorized.intValue
  val OK: Int = StatusCodes.OK.intValue
  val Created: Int = StatusCodes.Created.intValue
  val BadRequest: Int = StatusCodes.BadRequest.intValue
  val NoContent: Int = StatusCodes.NoContent.intValue
  val NotFound: Int = StatusCodes.NotFound.intValue
}
