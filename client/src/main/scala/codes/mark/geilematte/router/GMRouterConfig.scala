package codes.mark.geilematte.router

import codes.mark.geilematte.components.GameMenu
import codes.mark.geilematte.editor.QuestionCreator
import codes.mark.geilematte.html.Static
import codes.mark.geilematte.remote.LoginHandler
import codes.mark.geilematte.{Login, Register, SessionInfo, UserId}
import japgolly.scalajs.react.extra.router.{
  Redirect,
  Resolution,
  RouterConfigDsl,
  RouterCtl
}
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import org.scalajs.dom

sealed trait GMPage
case object LoginPage     extends GMPage
case object RegisterPage  extends GMPage
case object NotFoundPage  extends GMPage
case object ForbiddenPage extends GMPage
final case class GameMenuPage(userId: UserId, session: SessionInfo)
    extends GMPage
final case class EditorPage(userId: UserId, session: SessionInfo)
    extends GMPage
//final case class Encounter(userId: Int, session: String)    extends GMPages

object GMPage extends ParamParsers {

  val routerConfig = RouterConfigDsl[GMPage].buildConfig { dsl =>
    import dsl._

    implicit val method: Redirect.Method = Redirect.Push

    (emptyRule
      | staticRoute("login", LoginPage) ~> renderR(
        route =>
          Login.component(
            (id: UserId, info: SessionInfo) =>
              route.set(GameMenuPage(id, info))
        )
      )
      | staticRoute("register", RegisterPage) ~> renderR(
        route => Register.component(route.set(LoginPage))
      )
      | staticRoute("not_found", NotFoundPage) ~> render(
        Static.notFound
      )
      | staticRoute("forbidden", ForbiddenPage) ~> render(
        Static.forbidden
      )
      | dynamicRouteCT(
        ("menu" / userIdParam / sessionParam).caseClass[GameMenuPage]
      ) ~>
        dynRender(
          (gmp: GameMenuPage) => {
            GameMenu.component((gmp.userId, gmp.session))
          }
        )
      | dynamicRouteCT(
        ("creator" / userIdParam / sessionParam).caseClass[EditorPage]
      ) ~>
        dynRenderR( (ep:EditorPage, route) => {
            QuestionCreator.component((ep.userId, ep.session, route.set(ForbiddenPage)))
          }
        )).notFound(redirectToPage(NotFoundPage)(Redirect.Replace))
  }
}

trait ParamParsers {
  val userIdParam = new RouteB[UserId](
    "(-?\\d+)",
    1,
    g => Some(UserId(g(0).toInt)),
    _.id.toString
  )
  val sessionParam = new RouteB[SessionInfo](
    "(-?[a-zA-Z0-9]+)",
    1,
    g => Some(SessionInfo(g(0))),
    _.unboxed
  )
}
