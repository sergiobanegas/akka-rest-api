package core.persistence

import java.util.Date

trait BaseEntity {
  val id: Option[Long]
  val isDeleted: Option[Boolean]
  val createdAt: Option[Date]
  val updatedAt: Option[Date]
}
