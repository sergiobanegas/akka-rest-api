package infrastructure.model.response

import core.generic.GenericResponse
import core.http.model.StatusCode

case class CreatedResponse(code: Int = StatusCode.Created, message: String) extends GenericResponse
