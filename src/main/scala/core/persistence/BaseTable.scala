package core.persistence

import java.util.Date

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import scala.reflect._

abstract class BaseTable[E: ClassTag](tag: Tag, schemaName: Option[String], tableName: String)
  extends Table[E](tag, tableName) {
  implicit val dateMapper: JdbcType[Date] with BaseTypedType[Date] = MappedColumnType.base[Date, java.sql.Timestamp](
    d => new java.sql.Timestamp(d.getTime),
    d => new Date(d.getTime))

  val classOfEntity: Class[_] = classTag[E].runtimeClass

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def isDeleted = column[Option[Boolean]]("is_deleted", O.Default(Some(false)))

  def createdAt = column[Option[Date]]("created_at")

  def updatedAt = column[Option[Date]]("updated_at")
}
