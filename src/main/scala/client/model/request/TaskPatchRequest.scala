package client.model.request

import java.util.Date

case class TaskPatchRequest(title: Option[String], content: Option[String], effectiveDate: Option[Date], expirationDate: Option[Date])
