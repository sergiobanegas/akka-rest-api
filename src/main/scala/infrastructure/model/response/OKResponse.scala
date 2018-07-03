package infrastructure.model.response

import core.generic.GenericResponse
import core.http.model.StatusCode

case class OKResponse(code: Int = StatusCode.OK, message: String) extends GenericResponse
