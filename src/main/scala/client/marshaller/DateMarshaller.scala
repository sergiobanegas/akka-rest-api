package client.marshaller

import java.text._
import java.util._

import client.model.ErrorCodes
import client.model.response.BadRequestResponse
import core.http.util.JsonParser

import scala.util.Try
import spray.json._

object DateMarshaller {

  implicit object DateFormat extends JsonFormat[Date] {

    def write(date: Date) = JsString(dateToIsoString(date))

    def read(json: JsValue): Date = json match {
      case JsString(rawDate) =>
        val error = ErrorCodes.getMessageData(ErrorCodes.WrongDateFormat, Array(rawDate))
        parseIsoDateString(rawDate)
          .fold(deserializationError(JsonParser.toJson(BadRequestResponse(errorCode = error._1, message = error._2))))(identity)
      case _ => null
    }
  }

  private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
    override def initialValue() = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  }

  private def dateToIsoString(date: Date) = localIsoDateFormatter.get().format(date)

  private def parseIsoDateString(date: String): Option[Date] =
    Try {
      localIsoDateFormatter.get().parse(date)
    }.toOption
}