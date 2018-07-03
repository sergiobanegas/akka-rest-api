package infrastructure.model.dao.table

import core.persistence.BaseTable
import infrastructure.model.dao.Role
import slick.jdbc.PostgresProfile.api._

class RoleTable(tag: Tag) extends BaseTable[Role](tag, Some("roles"), "roles") {

  def name = column[String]("name")

  def * = (id.?, name, isDeleted, createdAt, updatedAt) <> ((Role.apply _).tupled, Role.unapply)
}
