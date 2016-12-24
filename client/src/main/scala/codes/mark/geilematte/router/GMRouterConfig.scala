package codes.mark.geilematte.router

import codes.mark.geilematte.components.GameMenu
import codes.mark.geilematte.{Login, Register, SessionInfo, UserId}
import japgolly.scalajs.react.extra.router.{
  Redirect,
  Resolution,
  RouterConfigDsl,
  RouterCtl
}
import japgolly.scalajs.react.Callback

sealed trait GMPage
case object LoginPage                                       extends GMPage
case object RegisterPage                                    extends GMPage
final case class GameMenuPage(userId: Int, session: String) extends GMPage
case object EditorPage                                      extends GMPage
//final case class Encounter(userId: Int, session: String)    extends GMPages

object GMPage {
  val routerConfig = RouterConfigDsl[GMPage].buildConfig { dsl =>
    import dsl._

    implicit val method: Redirect.Method = Redirect.Push

    (emptyRule
      | staticRoute("login", LoginPage) ~> render(
        Login.component(
          (id: UserId, info: SessionInfo) =>
            Callback(
              redirectToPage(GameMenuPage(id.id, info.unboxed))
          )
        ))
      | staticRoute("register", RegisterPage) ~> render(
        Register.component(
          Callback(
            redirectToPage(LoginPage)
          )
        )
      )
      | dynamicRouteCT(
        ("menu" / int / string("[a-zA-Z0-9]+")).caseClass[GameMenuPage]) ~>
        dynRender((gmp: GameMenuPage) => GameMenu.component(gmp)))
      .notFound(redirectToPage(LoginPage)(Redirect.Replace))
//      .renderWith(layout)
  }
  def layout(c: RouterCtl[GMPage], r: Resolution[GMPage]) = r.render()
}
