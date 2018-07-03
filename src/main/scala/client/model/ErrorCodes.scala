package client.model

object ErrorCodes {
  val WrongDateFormat = "wrong.date.format"

  def getMessageData(key: String, args: Array[String] = Array.empty): (String, String) = {
    (key, getMessage(key, args))
  }

  def getMessage(code: String, args: Array[String] = Array.empty): String = code match {
    case WrongDateFormat => "Wrong date format, expected ISO format, get " + args(0)
    case _ => ""
  }

}
