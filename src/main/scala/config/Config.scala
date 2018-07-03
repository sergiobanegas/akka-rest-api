package config

import com.typesafe.config.ConfigFactory
import slick.jdbc.PostgresProfile.api._

trait Config {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val databaseConfig = config.getConfig("database")
  val httpInterface: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")

  val databaseUrl: String = databaseConfig.getString("url")
  val databaseUser: String = databaseConfig.getString("user")
  val databasePassword: String = databaseConfig.getString("password")

  val db = Database.forConfig("database")

  private val securityConfig = config.getConfig("security")
  val JWT_COOKIE: String = securityConfig.getString("cookie")
  val JWT_SECRET_KEY: String = securityConfig.getString("secretKey")
  val JWT_ALGORITHM: String = securityConfig.getString("algorithm")
  val JWT_EXPIRY_CLAIM: String = securityConfig.getString("expiryClaim")
  val JWT_EXPIRY_PERIOD: Int = securityConfig.getInt("expiryPeriod")
  val JWT_USER_ID_CLAIM: String = securityConfig.getString("userIdClaim")
  val JWT_ROLE_CLAIM: String = securityConfig.getString("roleClaim")

}
