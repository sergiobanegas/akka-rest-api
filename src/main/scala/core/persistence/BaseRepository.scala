package core.persistence

import java.sql.SQLException

import config.Config
import core.persistence.error.DBError
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{CanBeQueryCondition, Rep, TableQuery}
import slick.sql.FixedSqlAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect._

trait BaseRepositoryComponent[T <: BaseTable[E], E <: BaseEntity] {
  def findOne(id: Long): Future[Either[DBError, Option[E]]]

  def findAll: Future[Either[DBError, Seq[E]]]

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Either[DBError, Seq[E]]]

  def filterOne[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Either[DBError, Option[E]]]

  def save(row: E): Future[Either[DBError, E]]

  def deleteById(id: Long): Future[Either[DBError, Int]]

  def updateById(id: Long, row: E): Future[Either[DBError, Int]]
}

trait BaseRepositoryQuery[T <: BaseTable[E], E <: BaseEntity] {

  val query: slick.jdbc.PostgresProfile.api.type#TableQuery[T]

  def getByIdQuery(id: Long): Query[T, E, Seq] = {
    query.filter(_.id === id).filter(_.isDeleted === false)
  }

  def getAllQuery: Query[T, E, Seq] = {
    query.filter(_.isDeleted === false)
  }

  def filterQuery[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Query[T, E, Seq] = {
    query.filter(expr).filter(_.isDeleted === false)
  }

  def saveQuery(row: E): FixedSqlAction[E, NoStream, Effect.Write] = {
    query returning query += row
  }

  def deleteByIdQuery(id: Long): FixedSqlAction[Int, NoStream, Effect.Write] = {
    query.filter(_.id === id).map(_.isDeleted).update(Some(true))
  }

  def updateByIdQuery(id: Long, row: E): FixedSqlAction[Int, NoStream, Effect.Write] = {
    query.filter(_.id === id).filter(_.isDeleted === false).update(row)
  }

}

abstract class BaseRepository[T <: BaseTable[E], E <: BaseEntity : ClassTag](clazz: TableQuery[T]) extends BaseRepositoryQuery[T, E] with BaseRepositoryComponent[T, E] with Config {

  val table: TableQuery[T] = clazz
  lazy val entity: Class[_] = classTag[E].runtimeClass
  val query: slick.jdbc.PostgresProfile.api.type#TableQuery[T] = clazz

  def findAll: Future[Either[DBError, Seq[E]]] = {
    mapResult(db.run(getAllQuery.result))
  }

  def findOne(id: Long): Future[Either[DBError, Option[E]]] = {
    mapResult(db.run(getByIdQuery(id).result.headOption))
  }

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Either[DBError, Seq[E]]] = {
    mapResult(db.run(filterQuery(expr).result))
  }

  def filterOne[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Either[DBError, Option[E]]] = {
    mapResult(db.run(filterQuery(expr).result.headOption))
  }

  def save(row: E): Future[Either[DBError, E]] = {
    mapResult(db.run(saveQuery(row)))
  }

  def updateById(id: Long, row: E): Future[Either[DBError, Int]] = {
    mapResult(db.run(updateByIdQuery(id, row)))
  }

  def deleteById(id: Long): Future[Either[DBError, Int]] = {
    mapResult(db.run(deleteByIdQuery(id)))
  }

  def mapResult[R](queryResult: Future[R]): Future[Either[DBError, R]] = {
    queryResult.map(res => Right(res)).recoverWith {
      case ex: SQLException =>
        Future.successful(Left(DBError(ex)))
    }
  }

}
