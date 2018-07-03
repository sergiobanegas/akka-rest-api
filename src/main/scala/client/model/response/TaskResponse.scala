package client.model.response

import java.util.Date

import domain.model.TaskDetails
import domain.IdTypes.TaskId

case class TaskResponse(id: TaskId, title: String, content: String, effectiveDate: Date, expirationDate: Date, createdAt: Date, updatedAt: Date) {
  def this(task: TaskDetails) {
    this(task.id.get, task.title, task.content, task.effectiveDate, task.expirationDate.orNull, task.createdAt.get, task.updatedAt.get)
  }
}
