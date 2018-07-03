package config

import org.flywaydb.core.Flyway

trait MigrationConfig extends Config {

  private val flyway = new Flyway()
  flyway.setValidateOnMigrate(false)
  flyway.setDataSource(databaseUrl, databaseUser, databasePassword)

  def migrate(): Int = {
    flyway.migrate()
  }

  def reloadSchema(): Int = {
    flyway.clean()
    flyway.migrate()
  }

}
