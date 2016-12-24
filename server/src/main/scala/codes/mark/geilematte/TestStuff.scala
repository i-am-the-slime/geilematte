package codes.mark.geilematte

import codes.mark.geilematte.config.HmacSecret
import codes.mark.geilematte.db.Database
import org.http4s.EntityEncoder
import org.http4s.EntityEncoder.Entity
import scodec._
import scodec.bits._
import scodec.codecs.implicits._
import org.http4s.blaze._
import org.http4s.dsl._

object TestStuff extends EntityEncoders with Database.Implicits {
  def test = {
    println("Hi guys :)")
    println(Database.rememberUserLogin(UserId(1))(HmacSecret("ABC")).task.run)
  }
}
