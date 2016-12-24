package codes.mark.geilematte.editor

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactElement, ReactNode}


final case class MakerProps()
object QuestionMaker {
    def component =
      ReactComponentB[MakerProps]("Question Maker Component").render_P {
        case MakerProps() =>
          <.span("category", <.select(
            <.option(^.value := "Sets")
          ))
    }
}
