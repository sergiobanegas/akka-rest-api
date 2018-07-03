package infrastructure.repository

import core.persistence.BaseRepository
import core.persistence.error.DBError
import domain.IdTypes.UserId
import infrastructure.model.dao.UserRole
import infrastructure.model.dao.table.UserRoleTable
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class UserRoleRepository extends BaseRepository[UserRoleTable, UserRole](TableQuery[UserRoleTable]) {

  private val DEFAULT_ROLE = 2L

  val roleRepository = new RoleRepository

  def getUserRole(userId: UserId): Future[Either[DBError, Option[String]]] = {
    val role = for {
      (userData, roleData) <- this.table.join(roleRepository.table).on(_.roleId === _.id)
      if userData.userId === userId
    } yield roleData.name
    mapResult(db.run(role.result.headOption))
  }

  def save(userId: UserId): Future[Either[DBError, UserRole]] ={
    super.save(UserRole(userId = userId, roleId = DEFAULT_ROLE))
  }

}
