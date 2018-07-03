package client.model.request

case class AccountPatchRequest(userName: Option[String], age: Option[Int], gender: Option[String])
