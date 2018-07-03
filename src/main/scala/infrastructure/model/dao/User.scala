package infrastructure.model.dao

import java.util.Date

import core.persistence.BaseEntity
import domain.IdTypes.UserId

case class User(id: Option[UserId] = None, email: String, userName: String, password: String, age: Int, gender: String, isDeleted: Option[Boolean] = Some(false), createdAt: Option[Date] = Some(new Date), updatedAt: Option[Date] = Some(new Date)) extends BaseEntity
