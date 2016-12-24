package codes.mark

import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor
import org.http4s.headers.`Content-Type`
import org.http4s.{EntityEncoder, MediaType}
import scodec.bits.ByteVector

import scalatags.Text.TypedTag
import scalaz.concurrent.Task
import scalaz.std.vector._
import scalaz.std.string._
import scalaz.syntax.traverse._

package object geilematte {
  trait GeileMatteEntityEncoders {
    implicit val htmlEnc: EntityEncoder[TypedTag[String]] =
      EntityEncoder.simple(`Content-Type`(MediaType.`text/html`))(
        tt => ByteVector(("<!DOCTYPE html>" + tt.render).getBytes)
      )

    implicit def encVectorCategory: EntityEncoder[Vector[Category]] =
      EntityEncoder.simple(`Content-Type`(MediaType.`application/json`))(
        (vec: Vector[Category]) => ByteVector(("[" + vec.foldMap(_.name + ",\n").dropRight(1) + "]").getBytes)
      )

    implicit val intEnc: EntityEncoder[Int] =
      EntityEncoder.simple(`Content-Type`(MediaType.`text/plain`))(
        (i: Int) => ByteVector(i.toString.getBytes)
      )

  }

  object Implicits extends GeileMatteEntityEncoders
}
