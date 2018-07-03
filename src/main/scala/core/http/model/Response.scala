package core.http.model

trait Response {
  val code: Int
  val message: String
}
