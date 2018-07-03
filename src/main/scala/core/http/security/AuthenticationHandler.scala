package core.http.security

import java.util.concurrent.TimeUnit

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import config.Config
import core.http.model.UserSessionDetails

trait AuthenticationHandler extends Config {

  protected def extractClaims(jwt: String): Option[Map[String, String]] = jwt match {
    case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption
    case _ => None
  }

  protected def generateClaims(user: UserSessionDetails, expiryPeriodInDays: Long) = JwtClaimsSet(
    Map(JWT_USER_ID_CLAIM -> user.id.get,
      JWT_ROLE_CLAIM -> user.role,
      JWT_EXPIRY_CLAIM -> (System.currentTimeMillis() + TimeUnit.DAYS
        .toMillis(expiryPeriodInDays)))
  )

  protected def isTokenExpired(jwt: String): Boolean = extractClaims(jwt) match {
    case Some(claims) =>
      claims.get(JWT_EXPIRY_CLAIM) match {
        case Some(value) => value.toLong < System.currentTimeMillis()
        case None => false
      }
    case None => false
  }

  protected def isValidToken(jwt: String): Boolean = JsonWebToken.validate(jwt, JWT_SECRET_KEY)

  protected def userHasRole(jwt: String, role: String): Boolean = extractClaims(jwt).get(JWT_ROLE_CLAIM).equals(role)

  protected def createAuthorizationHeader(user: UserSessionDetails): String = {
    JsonWebToken(JwtHeader(JWT_ALGORITHM), generateClaims(user, JWT_EXPIRY_PERIOD), JWT_SECRET_KEY)
  }

  protected def getUserId(claims: Map[String, Any]): Long = {
    claims(JWT_USER_ID_CLAIM) match {
      case value: String => value.toLong
      case _ => -1L
    }
  }

}
