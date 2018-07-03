package core.http.model

case class SuccessResponse(code: Int = StatusCode.OK, message: String) extends Response
