package codes.mark.geilematte.router

import codes.mark.geilematte.components.GameMenu
import codes.mark.geilematte.html.Static
import codes.mark.geilematte.{Login, Register, SessionInfo, UserId}
import japgolly.scalajs.react.extra.router.{
  Redirect,
  Resolution,
  RouterConfigDsl,
  RouterCtl
}
import japgolly.scalajs.react.Callback
import org.scalajs.dom

sealed trait GMPage
case object LoginPage                                       extends GMPage
case object RegisterPage                                    extends GMPage
case object NotFoundPage                                    extends GMPage
final case class GameMenuPage(userId: Int, session: String) extends GMPage
case object EditorPage                                      extends GMPage
//final case class Encounter(userId: Int, session: String)    extends GMPages

object GMPage {
  val routerConfig = RouterConfigDsl[GMPage].buildConfig { dsl =>
    import dsl._

    implicit val method: Redirect.Method = Redirect.Push

    (emptyRule
      | staticRoute("login", LoginPage) ~> renderR(
        route =>
          Login.component(
            (id: UserId, info: SessionInfo) =>
              route.set(GameMenuPage(id.id, info.unboxed))
        )
      )
      | staticRoute("register", RegisterPage) ~> renderR(
        route => Register.component(route.set(LoginPage))
      )
      | staticRoute("not_found", NotFoundPage) ~> render(
        Static.notFound
      )
      | dynamicRouteCT(
        ("menu" / int / string("[a-zA-Z0-9]+"))
          .caseClass[GameMenuPage]
      ) ~>
        dynRender(
          (gmp: GameMenuPage) => {
            GameMenu.component((UserId(gmp.userId), SessionInfo(gmp.session)))
          }
        ))
      .notFound(redirectToPage(NotFoundPage)(Redirect.Replace))
  }
}
