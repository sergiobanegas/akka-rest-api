package core.util

import org.mindrot.jbcrypt.BCrypt

object CryptService {

  def encrypt(str: String): String = BCrypt.hashpw(str, BCrypt.gensalt(12))

  def isValid(str: String, hashedStr: String): Boolean = BCrypt.checkpw(str, hashedStr)

}
