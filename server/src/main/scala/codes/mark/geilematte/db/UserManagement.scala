package codes.mark.geilematte.db

import codes.mark.geilematte.MattePermissions.{MatteAdmin, MatteEditor, MatteUser}
import codes.mark.geilematte._
import codes.mark.geilematte.config.HmacSecret
import com.roundeights.hasher.Implicits._
import codes.mark.geilematte.registration.RegistrationLink
import com.roundeights.hasher.Algo
import doobie.imports._

import scalaz.{Id, Maybe, Reader, \/}
import scalaz.syntax.either._
import scalaz.syntax.monad._
import scalaz.concurrent.Task

object UserManagement {

  sealed trait UserDBProblem
  object UserDBProblem {
    val notConfirmed:UserDBProblem = UserNotConfirmed
    val emailOrPasswordWrong:UserDBProblem = EmailOrPasswordWrong
  }
  final case object UserNotConfirmed     extends UserDBProblem
  final case object EmailOrPasswordWrong extends UserDBProblem
}

trait UserManagement {
  import UserManagement._

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

  def getSaltForUser(email: EmailAddress): ConnectionIO[Maybe[Salt]] = {
    val mail = email.toString
    sql"select salt from users where email = $mail"
      .query[String]
      .map(Salt(_))
      .option
      .map(Maybe.fromOption)
  }

  def checkUserPassword(email: EmailAddress,
                        password: EncryptedPassword): ConnectionIO[UserDBProblem \/ UserId] = {
    val mail = email.toString
    val pwd  = password.hashed
    sql"""select u_id, confirmed from users where email = $mail and password = $pwd"""
      .query[(Int, Boolean)]
      .map{
        case (id, isConfirmed) =>
          if(!isConfirmed) UserDBProblem.notConfirmed.left[UserId] else UserId(id).right[UserDBProblem]
      }
      .option
      .map(o => Maybe.fromOption(o).\/>(UserDBProblem.emailOrPasswordWrong).join
      )
  }

  def rememberUserLogin(uId:UserId): HmacSecret => ConnectionIO[SessionInfo] =
    (secret: HmacSecret) => {
      val cookieValue = Algo.hmac(secret.secret).sha256(uId.id.toString).hex
      val userId = uId.id
      sql"""
        update users set
        session = $cookieValue
        where u_id = $userId
      """.update.run.map(_ => SessionInfo(cookieValue))
    }

  def checkSession(uId:UserId, info:SessionInfo): ConnectionIO[Boolean] =  {
    val userId = uId.id
    val session = info.unboxed
    sql"""
      select count(*) from users where u_id = $userId and session = $session
    """.query[Int].map(_ == 1).unique
  }

  def canEdit(uId:UserId): ConnectionIO[Boolean] = {
    val userId = uId.id
    sql"""
      select permissions from users where u_id = $userId
    """.query[MattePermissions].map{
      case MatteEditor => true
      case MatteAdmin => true
      case MatteUser => false
    }.option.map(_.getOrElse(false))
  }

}
