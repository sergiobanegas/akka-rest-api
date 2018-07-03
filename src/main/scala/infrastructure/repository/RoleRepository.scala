package infrastructure.repository

import core.persistence.BaseRepository
import infrastructure.model.dao.Role
import infrastructure.model.dao.table.RoleTable
import slick.lifted.TableQuery

class RoleRepository extends BaseRepository[RoleTable, Role](TableQuery[RoleTable])
