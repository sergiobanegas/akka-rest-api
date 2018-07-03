package infrastructure.model.dao.table

import core.persistence.BaseTable
import infrastructure.model.dao.UserRole
import slick.jdbc.PostgresProfile.api._
import domain.IdTypes.UserId

class UserRoleTable(tag: Tag) extends BaseTable[UserRole](tag, Some("user_role"), "user_role") {

  def userId = column[UserId]("user_id")

  def roleId = column[Long]("role_id")

  def * = (id.?, userId, roleId, isDeleted, createdAt, updatedAt) <> ((UserRole.apply _).tupled, UserRole.unapply)
}
