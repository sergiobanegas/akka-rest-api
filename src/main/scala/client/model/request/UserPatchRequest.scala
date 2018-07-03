package client.model.request

case class UserPatchRequest(email: Option[String], password: Option[String], userName: Option[String], age: Option[Int], gender: Option[String])
