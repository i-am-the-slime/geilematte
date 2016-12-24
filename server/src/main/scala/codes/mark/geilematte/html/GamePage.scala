package codes.mark.geilematte.html

import scalatags.Text.all.{head => _head, html => _html, _}

object GamePage {
  val html = _html(
    _head(
      title := "Geile Matte, Alter",
//      link(rel := "stylesheet", href := "https://fonts.googleapis.com/css?family=Delius+Unicase"),
//      link(rel := "stylesheet", media := "screen", href := s"public/css/main.less"),
      script(src := "public/assets/js/melonJS-min.js")
    ),
    body(
      div(id := "react-container"),
      script(src := "public/client-jsdeps.js"),
      script(src := "public/client-fastopt.js"),
      script(src := "public/client-launcher.js")
    )
  )
}
