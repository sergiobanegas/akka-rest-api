package core.http.error

case class GenericErrorResponse(code: Int, errorCode: String, message: String) extends ErrorResponse
