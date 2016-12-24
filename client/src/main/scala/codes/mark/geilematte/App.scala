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
    val router = Router(BaseUrl.fromWindowOrigin / "#/", GMPage.routerConfig.logToConsole)
    router() render container
  }
//    val path      = window.location.pathname
//    path match {
//      case "/" =>
//        logInOrMain
//      case "/add_category" =>
//        ReactDOM.render(CategoryCreator.component(), container)
//      case "/question_editor" =>
//        ReactDOM.render(QuestionCreator.component(), container)
//    }
//
//    def logInOrMain = {
//      val userId = Cookies.read
//        .get("user_id")
//        .flatMap(
//          uid => \/.fromTryCatchNonFatal(UserId(uid.toInt)).toOption
//        )
//      userId.fold(logIn)(showMain)
//    }
//
//    def logIn: Unit = {
//      ReactDOM.render(
//        Register.component(
//          (uid: UserId) => Callback(showMain(uid))
//        ),
//        container
//      )
//      ()
//    }

//    def showMain(userId: UserId): Unit = {
//      Cookies.write(Map("user_id" -> userId.id.toString))
//
//      def question = GMClient.get[IdQ4]
//      //TODO: Tailrec
//      def work: Unit = question.runNow().onComplete {
//        case Success(q4) =>
//          ReactDOM.render(
//            QuestionAnswering.component(
//              NewGameProps(
//                q4,
//                correct =>
//                  Callback(
//                    window.alert(if (correct) "Richtig" else "Falsch")
//                  ) >> Callback(work))
//            ),
//            container
//          )
//      }
//      work
//    }
//  }
}
