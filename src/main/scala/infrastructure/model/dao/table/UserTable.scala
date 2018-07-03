package infrastructure.model.dao.table

import infrastructure.model.dao.User
import core.persistence.BaseTable
import slick.jdbc.PostgresProfile.api._

class UserTable(tag: Tag) extends BaseTable[User](tag, Some("users"), "users") {

  def email = column[String]("email")

  def username = column[String]("username")

  def password = column[String]("password")

  def age = column[Int]("age")

  def gender = column[String]("gender")

  def * = (id.?, email, username, password, age, gender, isDeleted, createdAt, updatedAt) <> ((User.apply _).tupled, User.unapply)
}
