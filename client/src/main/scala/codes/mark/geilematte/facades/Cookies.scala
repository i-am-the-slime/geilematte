package codes.mark.geilematte.facades

import org.scalajs.dom.document

import scala.scalajs.js.Date
import scalaz.std.list._

object Cookies {

  def read: Map[String, String] =
    document.cookie
      .split(";")
      .toList
      .map(_.split("=").toList)
      .flatMap(x =>
        (x.headOption, x.drop(1).headOption) match {
          case (Some(k), Some(v)) => List((k.trim, v))
          case _                  => Nil
        })
      .toMap

  def write(values:Map[String, String]): Unit = {
    val expiry = new Date(2020, 1)
    values.toList.foreach {
        case (k, v) =>
          val expires = expiry.toUTCString
          document.cookie = s"$k=$v;expires=$expires;path=/"
    }
  }
}
