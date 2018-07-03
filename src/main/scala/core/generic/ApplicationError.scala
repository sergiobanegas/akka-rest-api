package core.generic

trait ApplicationError {
  val code: Int
  val errorCode: String
  val message: String
  val exception: Exception = null
}
