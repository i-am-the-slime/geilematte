package codes.mark.geilematte.facades

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.annotation.JSName

@JSName("katex")
@js.native
object KaTex extends js.Object {
  def render(latex: String, target: dom.Element, config:js.Object = new KaTexConfig(true)): Unit = js.native
}

@js.native
class KaTexConfig(var displayMode:Boolean) extends js.Object

object SafeKaTex {
  def render(latex: String, target: dom.Element): Option[String] =
    try {
      KaTex.render(latex, target)
      Option.empty
    } catch {
      case (e: JavaScriptException) =>
        Option(e.getMessage)
    }
}
