package codes.mark.geilematte
package html

import scalatags.Text.all.{html => _html, head => _head, _}

object MainPage {
  val html = _html(
    _head(
      title := "Geile Matte, Alter",
      link(rel := "stylesheet", href := "https://fonts.googleapis.com/css?family=Delius+Unicase"),
      link(rel := "stylesheet", href := "https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.6.0/katex.min.css"),
      link(rel := "stylesheet", media := "screen", href := s"public/css/main.less"),
      script(src := "public/js/twin-bcrypt.min.js"),
      script(src := "https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.6.0/katex.min.js")
    ),
    body(
      div(id := "react-container"),
      script(src := "public/client-fastopt.js"),
      script(src := "public/client-jsdeps.js"),
      script(src := "public/client-launcher.js")
    )
  )
}
