package infrastructure.model.dao

import java.util.Date

import core.persistence.BaseEntity
import domain.IdTypes.RoleId

case class Role(id: Option[RoleId], name: String, isDeleted: Option[Boolean], createdAt: Option[Date], updatedAt: Option[Date]) extends BaseEntity
