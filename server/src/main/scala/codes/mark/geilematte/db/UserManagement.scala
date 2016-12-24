package codes.mark.geilematte.db

import codes.mark.geilematte._
import codes.mark.geilematte.config.HmacSecret
import com.roundeights.hasher.Implicits._
import codes.mark.geilematte.registration.RegistrationLink
import com.roundeights.hasher.Algo
import doobie.imports._

import scalaz.{Id, Reader}
import scalaz.concurrent.Task

trait UserManagement {

  //Drop all session data
  def initUsers = sql"update users set session = NULL".update.run

  def addUser(info: NewUserInfo,
              link: RegistrationLink): ConnectionIO[UserId] = {
    val email            = info.email.toString
    val password         = info.passwordWithSalt.password.hashed
    val salt             = info.passwordWithSalt.salt.str
    val registrationLink = link.link
    sql"""insert into
          users  (email , password ,  salt, registration_link)
          values ($email, $password, $salt, $registrationLink)""".update
      .withUniqueGeneratedKeys[Int]("u_id")
      .map(UserId(_))
  }

  def finishRegistration(link: RegistrationLink): ConnectionIO[Boolean] = {
    val l = link.link
    sql"""update users
      set confirmed = TRUE
      where registration_link = $l
      """.update.run.map(_ == 1)
  }

  def getSaltForUser(email: EmailAddress): ConnectionIO[Option[Salt]] = {
    val mail = email.toString
    sql"select salt from users where email = $mail"
      .query[String]
      .map(Salt(_))
      .option
  }

  def checkUserPassword(email: EmailAddress,
                        password: EncryptedPassword): ConnectionIO[Option[UserId]] = {
    val mail = email.toString
    val pwd  = password.hashed
    sql"""select u_id
      from users
      where email = $mail and password = $pwd"""
      .query[Int]
      .map(UserId(_))
      .option
  }

  def rememberUserLogin(uId:UserId): HmacSecret => ConnectionIO[SessionInfo] =
    (secret: HmacSecret) => {
      val cookieValue = Algo.hmac(secret.secret).sha256(uId.id.toString).hex
      sql"""
        update users set
        session = $cookieValue
        where u_id = ${uId.id}
      """.update.run.map(_ => SessionInfo(cookieValue))
    }

}
