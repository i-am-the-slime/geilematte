package codes.mark.geilematte.html

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.vdom.prefix_<^._

object Static {
  val notFound: ReactElement =
    <.div(^.height := "100%", ^.overflowY := "hidden")(
      <.h1("404"), fullSizeImage("not_found.jpg")
    )

  val forbidden: ReactElement =
    <.div(^.height := "100%", ^.overflowY := "hidden")(
      <.h1("Verboten!"), fullSizeImage("forbidden.gif")
    )

  def fullSizeImage(name:String) = {
    <.div(
      ^.backgroundImage := s"url('/public/img/${name}')",
      ^.cls := "fullSizeImage"
    )
  }
}
