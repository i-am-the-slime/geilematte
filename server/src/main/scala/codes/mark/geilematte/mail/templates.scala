package codes.mark.geilematte.mail

import codes.mark.geilematte.EmailAddress
import codes.mark.geilematte.config.ThisServersUri
import codes.mark.geilematte.registration.RegistrationLink
import scalatags.Text.all._

object templates {
  def welcome(
      recipient: EmailAddress,
      registrationLink: RegistrationLink): ThisServersUri => Email =

    (serverUri:ThisServersUri) => {
      val link = serverUri.uri / "api" / "finish_registration" / registrationLink.link
      Email(
        recipient,
        "Krasse Friese, Alter!",
        div(
          h1("Willkommen in der wunderbaren Welt der geilen Matten!"),
          p("Jetzt kann es fast los gehen. Nur noch ein Klick trennt dich von Dauerwellen und Halbglatzen."),
          p("Und hier ist er auch schon: ",
            a(href := link.renderString)(link.renderString)
          ),
          p("Viel Spaß beim Föhnen"),
          h2("Mark"),
          p("PS: Falls du nicht mit dieser Email gerechnet haben solltest, dann lösche sie bitte einfach, Entschuldigung.")
        )
      )
    }
}
