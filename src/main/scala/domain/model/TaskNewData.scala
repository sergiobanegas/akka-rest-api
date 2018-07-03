package domain.model

import java.util.Date

case class TaskNewData(title: Option[String], content: Option[String], effectiveDate: Option[Date], expirationDate: Option[Date])
