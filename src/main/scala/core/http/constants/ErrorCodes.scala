package core.http.constants

object ErrorCodes {
  val Forbidden = "forbidden"
  val ExpiredToken = "expired.token"
  val InvalidToken = "invalid.token"
  val InternalServerError = "internal.server.error"
  val MissingToken = "auth.token.missing"

  def getMessageData(key: String, args: Array[String] = Array.empty): (String, String) = {
    (key, getMessage(key, args))
  }

  def getMessage(code: String, args: Array[String] = Array.empty): String = code match {
    case Forbidden => "Forbidden access"
    case ExpiredToken => "The token has expired"
    case InvalidToken => "Invalid token"
    case InternalServerError => "Internal server error"
    case MissingToken => "Missing authorization token"
    case _ => ""
  }

}
