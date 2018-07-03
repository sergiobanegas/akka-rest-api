package infrastructure.model.dao

import java.util.Date

import core.persistence.BaseEntity
import domain.IdTypes.{TaskId, UserId}

case class Task(id: Option[TaskId] = None, userId: UserId, title: String, content: String, effectiveDate: Date, expirationDate: Option[Date] = None, isDeleted: Option[Boolean] = Some(false), createdAt: Option[Date] = Some(new Date), updatedAt: Option[Date] = Some(new Date)) extends BaseEntity
