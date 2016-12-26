package codes.mark.geilematte.editor

import codes.mark.geilematte.components.Matte
import codes.mark.geilematte._
import codes.mark.geilematte.remote.GMClient
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactElement, ReactEventI, ReactNode}
import monocle.Lens
import monocle.macros.Lenses
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

@Lenses("_") final case class QuestionCreatorState(qInput: String,
                                                   previewingQuestion: Boolean,
                                                   correctInput: String,
                                                   previewingCorrect: Boolean,
                                                   w1Input: String,
                                                   previewingW1: Boolean,
                                                   w2Input: String,
                                                   previewingW2: Boolean,
                                                   w3Input: String,
                                                   previewingW3: Boolean,
                                                   result: Option[Int]) {
  def toQ4 = Q4(
    1,
    Category("Sets"),
    LatexText(this.qInput),
    Ans(LatexText(this.correctInput)),
    Ans(LatexText(this.w1Input)),
    Ans(LatexText(this.w2Input)),
    Ans(LatexText(this.w3Input))
  )
}
object QuestionCreator {
  import QuestionCreatorState._
  def component =
    ReactComponentB[(UserId, SessionInfo, Callback)]("Question Maker Component")
      .initialState(
        QuestionCreatorState(
          "Question",
          false,
          "Correct Answer",
          false,
          "Wrong Answer 1",
          false,
          "Wrong Answer 2",
          false,
          "Wrong Answer 3",
          false,
          None
        ))
      .renderPS {
        case (scope, props, state) => {

          def clicked(e: ReactEventI): Callback = {
            e.preventDefaultCB >>
              scope.modState(_.copy(result = None)) >>
              GMClient.post[NewQuestionPost, IdQ4](NewQuestionPost(state.toQ4, props._1, props._2)) >>=~
              (_.fold(
                err => props._3,
                (idQ4: IdQ4) => scope.modState(_.copy(result = Option(idQ4.id)))
              )
              )
          }

          def changePreview(l: Lens[QuestionCreatorState, Boolean], t: Lens[QuestionCreatorState, String]) = {
            val arrayPrefix = """\begin{array}{l}"""
            val arrayPostfix = """\end{array}"""
            def changeToArray(e:ReactEventI) = {
              val newInput = e.target.value
              val arrayed: String =
                if (newInput.contains('\n')) arrayPrefix + newInput.replaceAll("\n", """\\\\""") + arrayPostfix
                else newInput
              scope.modState(t.set(arrayed))
            }
            def changeFromArray(s:String) = {
              s.stripPrefix(arrayPrefix).stripSuffix(arrayPostfix).replaceAll("""\\\\""", "\n")
            }
            if (l.get(state))
              <.div(
                ^.onClick -->
                  scope.modState(l.set(false)))(Matte.component(LatexText(t.get(state))))
            else
              <.div(
                <.textarea(
                  ^.`type` := "text",
                  ^.autoFocus := true,
                  ^.value := changeFromArray(t.get(state)),
                  ^.onBlur --> scope.modState(l.set(true)),
                  ^.onChange ==> changeToArray
                )
              )
          }

          <.div(^.cls := "enemy")(
            <.div(^.cls := "enemyImage",
                  ^.backgroundImage := "url('/public/img/cow.gif')"),
            <.div(^.cls := "questionAndAnswer")(
              <.div(^.cls := "question")(
                changePreview(_previewingQuestion, _qInput)
              ),
              <.div(^.cls := "answer")(
                changePreview(_previewingCorrect, _correctInput)
              ),
              <.div(^.cls := "answer")(
                changePreview(_previewingW1, _w1Input)
              ),
              <.div(^.cls := "answer")(
                changePreview(_previewingW2, _w2Input)
              ),
              <.div(^.cls := "answer")(
                changePreview(_previewingW3, _w3Input)
              )
            ),
            <.div(^.position := "absolute", ^.left := "15px", ^.top := "15px")(
              <.span("Ready:"),
              <.button(^.onClick ==> clicked)("Create"),
              state.result.fold(<.div())(res => <.div(res))
            )
          )
        }
      }
      .build
}
