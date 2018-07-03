package core.http.error

import core.http.model.StatusCode

case class UnauthorizedErrorResponse(code: Int = StatusCode.Unauthorized, errorCode: String, message: String) extends ErrorResponse
