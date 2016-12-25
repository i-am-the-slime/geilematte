package codes.mark.geilematte

import scalaz.{Maybe, \/}
import scalaz.Maybe.optionMaybeIso._

final case class EmailAddress private(beforeAt:String, afterAt:String, tld:String) {
  override def toString = s"$beforeAt@$afterAt.$tld"
}
object EmailAddress {
  def fromString(str:String):Maybe[EmailAddress] = {
    Maybe.fromTryCatchNonFatal {
      val Array(beforeAt, after) = str.split("@")
      after.split("\\.").reverse.toList match {
        case tld :: rest => EmailAddress(beforeAt, rest.reverse.mkString("."), tld)
      }
    }
  }
}

final case class EncryptedPassword(hashed:String) extends AnyVal

final case class Salt(str:String) extends AnyVal

final case class PasswordWithSalt(password:EncryptedPassword, salt:Salt)

final case class NewUserInfo(email:EmailAddress, passwordWithSalt: PasswordWithSalt)

final case class LoginAttempt(email:EmailAddress, passwordWithSalt:PasswordWithSalt)

final case class UserId(id:Int) extends AnyVal
