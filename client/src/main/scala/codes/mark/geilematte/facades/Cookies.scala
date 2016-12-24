package codes.mark.geilematte.facades

import org.scalajs.dom.document

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

  def write(values:Map[String, String]): Unit =
    document.cookie =
    intersperse(values.toList.map{
      case (k,v) => s"$k=$v"
    }, ";").mkString
}
