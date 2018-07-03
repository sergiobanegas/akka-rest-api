package domain.model

import java.util.Date

import domain.IdTypes.TaskId

case class TaskDetails(id: Option[TaskId], title: String, content: String, effectiveDate: Date, expirationDate: Option[Date], createdAt: Option[Date], updatedAt: Option[Date])
