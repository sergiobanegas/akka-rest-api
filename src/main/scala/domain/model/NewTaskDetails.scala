package domain.model

import java.util.Date

case class NewTaskDetails(title: String, content: String, effectiveDate: Date, expirationDate: Option[Date])
