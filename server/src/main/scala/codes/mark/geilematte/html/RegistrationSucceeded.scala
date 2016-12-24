package codes.mark.geilematte.html

import scalatags.Text.all.{head => _head, html => _html, _}

object RegistrationSucceeded {
  def html() = _html(
    _head( title := "Jetzt aber los hier!" ),
    body(
      div(
        h1("Danke")
      )
    )
  )
}
