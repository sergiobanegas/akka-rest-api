package domain.model

import java.util.Date

import domain.IdTypes.UserId

case class UserDetails(id: Option[UserId], email: String, userName: String, age: Int, gender: String, createdAt: Option[Date], updatedAt: Option[Date])
