package core.http.error

import core.http.constants.ErrorCodes
import core.http.model.StatusCode

case class InternalServerErrorResponse(code: Int = StatusCode.InternalServerError, errorCode: String = ErrorCodes.InternalServerError, message: String = ErrorCodes.getMessage(ErrorCodes.InternalServerError)) extends ErrorResponse
