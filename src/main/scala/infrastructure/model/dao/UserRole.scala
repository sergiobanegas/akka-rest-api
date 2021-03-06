package infrastructure.model.dao

import java.util.Date

import core.persistence.BaseEntity
import domain.IdTypes.{RoleId, UserId}

case class UserRole(id: Option[UserId] = None, userId: UserId, roleId: RoleId, isDeleted: Option[Boolean] = Some(false), createdAt: Option[Date] = Some(new Date), updatedAt: Option[Date] = Some(new Date)) extends BaseEntity
