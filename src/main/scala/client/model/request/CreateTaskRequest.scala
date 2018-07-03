package client.model.request

import java.util.Date

case class CreateTaskRequest(title: String, content: String, effectiveDate: Date, expirationDate: Option[Date])
