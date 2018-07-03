package infrastructure.model.dao.table

import java.util.Date

import core.persistence.BaseTable
import infrastructure.model.dao.Task
import slick.jdbc.PostgresProfile.api._
import domain.IdTypes.UserId

class TaskTable(tag: Tag) extends BaseTable[Task](tag, Some("tasks"), "tasks") {

  def userId = column[UserId]("user_id")

  def title = column[String]("title")

  def content = column[String]("content")

  def effectiveDate = column[Date]("effective_date")

  def expirationDate = column[Option[Date]]("expiration_date")

  def * = (id.?, userId, title, content, effectiveDate, expirationDate, isDeleted, createdAt, updatedAt) <> ((Task.apply _).tupled, Task.unapply)
}
