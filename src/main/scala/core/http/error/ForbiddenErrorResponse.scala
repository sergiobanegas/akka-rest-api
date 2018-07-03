package core.http.error

import core.http.constants.ErrorCodes
import core.http.model.StatusCode

case class ForbiddenErrorResponse(code: Int = StatusCode.Forbidden, errorCode: String = ErrorCodes.Forbidden, message: String = ErrorCodes.getMessage(ErrorCodes.Forbidden)) extends ErrorResponse
