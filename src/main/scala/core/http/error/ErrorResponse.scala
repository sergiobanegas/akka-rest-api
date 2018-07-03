package core.http.error

trait ErrorResponse {
  val code: Int
  val errorCode: String
  val message: String
}
