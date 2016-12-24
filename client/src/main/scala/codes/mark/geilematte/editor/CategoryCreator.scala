package codes.mark.geilematte
package editor

import codes.mark.geilematte.Category
import codes.mark.geilematte.remote.GMClient
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactElement, ReactEventI, ReactNode}
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

final case class CategoryCreatorState(inputText:String, result:Option[Int])
object CategoryCreator {
  def component =
    ReactComponentB[Unit]("Category Maker Component")
      .initialState(CategoryCreatorState("", None))
      .renderS( (scope, ccs) => {

        def clicked(e: ReactEventI): Callback = {
          e.preventDefaultCB >>
          scope.modState(_.copy(result = None)) >>
          GMClient.post[Category, Int](Category(ccs.inputText)) logErrors
            ((id:Int) => scope.modState(_.copy(result = Option(id))))
        }

        def changed(e: ReactEventI): Callback = {
          val newInput = e.target.value
          scope.modState(_.copy(inputText = newInput))
        }

        <.span(
          <.span("Enter a new category name"),
          <.input(^.`type` := "text", ^.value := ccs.inputText, ^.onChange ==> changed),
          <.button(^.onClick ==> clicked)("Create"),
          ccs.result.fold(<.div())(res => <.div(res))
        )
      })
      .build
}
