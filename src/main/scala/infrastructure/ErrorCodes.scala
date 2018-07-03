package infrastructure

object ErrorCodes {
  val WrongCredentials = "wrong.credentials"
  val EmailAlreadyExists = "user.with.email.already.exists"
  val UserNotExists = "user.not.exists"
  val TaskNotExists = "task.not.exists"
  val InternalServerError = "internal.server.error"
  val ExpirationDateBeforeOrSameEffectiveDate = "expiration.date.before.effective.date"
  val InvalidAge = "invalid.age"
  val InvalidGender = "invalid.gender"
  val WrongPassword = "wrong.password"
  val WrongOldPassword = "wrong.old.password"

  def getMessageData(key: String, args: Array[String] = Array.empty): (String, String) = {
    (key, getMessage(key, args))
  }

  def getMessage(code: String, args: Array[String] = Array.empty): String = code match {
    case WrongCredentials => "Wrong credentials"
    case EmailAlreadyExists => "There's already an existing user with the email " + args(0)
    case UserNotExists => "The user doesn't exist"
    case InternalServerError => "Internal server error"
    case TaskNotExists => "The task doesn't exist"
    case ExpirationDateBeforeOrSameEffectiveDate => "The expiration date can't be equals or be placed before the effective date"
    case InvalidAge => "The age is invalid, must be between 1 and 125"
    case InvalidGender => "The gender is invalid, must be 'F' or 'M'"
    case WrongPassword => "The password is invalid"
    case WrongOldPassword => "The old password is invalid"
    case _ => ""
  }

}
