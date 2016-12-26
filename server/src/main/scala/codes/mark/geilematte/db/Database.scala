package codes.mark.geilematte
package db

import codes.mark.geilematte._
import codes.mark.geilematte.config.ThisServersUri
import codes.mark.geilematte.mail.MailTransport
import codes.mark.geilematte.registration.RegistrationLink
import doobie.imports._
import doobie.postgres.imports._
import doobie.postgres._
import doobie.postgres.pgtypes._
import com.roundeights.hasher.Implicits._
import com.roundeights.hasher.{Hasher, Algo}
import scala.language.postfixOps
import scalaz.syntax.applicative._

import scala.util.Random
import scalaz.Reader
import scalaz.concurrent.Task

object Database extends UserManagement with Editor with Fights {

  val xa = DriverManagerTransactor[Task](
    //TODO: Change on server
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "meibes",
    ""
  )

  trait Implicits {
    implicit class TaskableTransaction[A](cio: ConnectionIO[A]) {
      lazy val task: Task[A] = cio.transact[Task](xa)
    }
  }

}
