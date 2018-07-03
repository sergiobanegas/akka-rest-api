package client.model.response

import core.generic.ApplicationError
import core.http.model.StatusCode

case class BadRequestResponse(code: Int = StatusCode.BadRequest, errorCode: String, message: String) extends ApplicationError
