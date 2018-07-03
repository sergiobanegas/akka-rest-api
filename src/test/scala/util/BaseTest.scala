package util

import config.MigrationConfig
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

trait BaseTest extends MigrationConfig with FlatSpecLike with BeforeAndAfterAll {

  override def beforeAll: Unit = {
    super.reloadSchema()
    super.migrate()
  }

  override def afterAll: Unit = {
    super.reloadSchema()
  }

}
