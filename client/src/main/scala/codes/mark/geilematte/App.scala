package codes.mark.geilematte

import codes.mark.geilematte.components.{Matte, NewGameProps, QuestionAnswering}
import codes.mark.geilematte.editor.{CategoryCreator, QuestionCreator}
import codes.mark.geilematte.facades.Cookies
import japgolly.scalajs.react.{CallbackTo, ReactDOM, ReactElement}
import codes.mark.geilematte.remote.{GMClient, Gettables}
import codes.mark.geilematte.router.{GMPage}

import scala.scalajs.js
import org.scalajs.dom.{document, window}
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import org.scalajs.dom
import scodec.codecs.implicits._

import scalaz.std.option._
import scalaz.syntax.std.option._
import scalaz.syntax.maybe._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scalaz.\/
import scalaz.Maybe

object App extends js.JSApp with Gettables {
  def main(): Unit = {

    //React
    val container = document.getElementById("react-container")
    val router = Router(BaseUrl.fromWindowOrigin / "#/", GMPage.routerConfig)
    router() render container
  }
}
