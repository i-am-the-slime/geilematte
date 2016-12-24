package codes.mark.geilematte.components

import codes.mark.geilematte.components.Matte
import codes.mark.geilematte.{Ans, AnswerId, Category, Guess, IdQ4, LatexText, Q4, Question4Id}
import codes.mark.geilematte.remote.{GMClient, Postables, RemoteError}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactElement, ReactEventI, ReactNode}
import monocle.Lens
import monocle.macros.Lenses
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Random, Success}

@Lenses("_") final case class NewGameProps(question: IdQ4, responded: Boolean => Callback)
object QuestionAnswering extends Postables with GMClient.Implicits {
  import NewGameProps._

  def component =
    ReactComponentB[NewGameProps]("Question Maker Component").render_P { props =>
      {

        def clicked(guess: Guess)(e: ReactEventI): Callback = {
          e.preventDefaultCB >>
            GMClient.post[Guess, Boolean](guess).logErrors(props.responded)
        }

        <.div(^.cls := "enemy")(
          <.div(^.cls := "enemyImage",
                ^.backgroundImage := "url('http://themetapicture.com/media/funny-gif-boy-soda-can-hair-dog.gif')"),
          <.div(^.cls := "questionAndAnswer")(
            <.div(^.cls := "question", ^.key := -234)(
              <.div(Matte.component(props.question.questionText))
            ) ::
              Random
                .shuffle(
                  List(
                    props.question.answer1,
                    props.question.answer2,
                    props.question.answer3,
                    props.question.answer4
                  )
                )
                .map(
                  idAns =>
                    <.div(^.cls := "answer",
                          ^.key := idAns.id,
                          ^.onClick ==> clicked(
                            Guess(
                              AnswerId(idAns.id),
                              Question4Id(props.question.id)
                            )
                          ))(
                      <.div(Matte.component(idAns.answerText))
                  )): _*
          )
        )
      }
    }.build
}
