package codes.mark.geilematte.components

import codes.mark.geilematte.components.Matte
import codes.mark.geilematte.{Ans, AnswerId, Category, Guess, IdQ4, LatexText, Q4, Question4Id, SessionInfo, UserId}
import codes.mark.geilematte.remote.{GMClient, Postables}
import codes.mark.geilematte.router.GameMenuPage
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactElement, ReactEventI, ReactNode}
import monocle.Lens
import monocle.macros.Lenses
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Random, Success}

object GameMenu {

  def component =
    ReactComponentB[(UserId, SessionInfo)]("Question Maker Component").render_P(props =>
      <.div(s"I am holding the place ${props._1} ${props._2}")
    ).build
}
