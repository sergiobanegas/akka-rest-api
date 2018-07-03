package client.model.response

import java.util.Date

import domain.model.UserDetails
import domain.IdTypes.UserId

case class UserResponse(id: UserId, email: String, userName: String, age: Int, gender: String, createdAt: Date, updatedAt: Date) {
  def this(user: UserDetails) {
    this(user.id.get, user.email, user.userName, user.age, user.gender, user.createdAt.get, user.updatedAt.get)
  }
}
