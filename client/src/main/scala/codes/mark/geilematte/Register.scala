package codes.mark.geilematte

import codes.mark.geilematte.html.Animation
import codes.mark.geilematte.remote.{Conflict, GMClient, Postables}
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js.timers._
import japgolly.scalajs.react.{
  Callback,
  CallbackTo,
  ReactComponentB,
  ReactEventI
}
import scala.concurrent.Promise
import monocle.macros.Lenses

import scalaz.{-\/, Maybe, \/, \/-}
import scala.concurrent.Future
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

@Lenses("_") final case class RegisterState(emailInput: String,
                                            passwordInput: String,
                                            emailError: Maybe[String],
                                            passwordInvalid: Boolean,
                                            loading: Boolean,
                                            sendingEmail: Boolean)

object Register extends Postables with GMClient.Implicits {

  def validatePassword(s: String): Maybe[String] = {
    if (s.length >= 8) Maybe.just(s) else Maybe.empty
  }

  import RegisterState._
  def component =
    ReactComponentB[Callback]("Log In Component")
      .initialState(RegisterState("", "", Maybe.empty, false, false, false))
      .renderPS { ($, callback, state) =>
        if (state.sendingEmail) {
          <.div(
            <.h1("Und jetzt..."),
            <.p(
              s"Check jetzt mal deine Email ${state.emailInput} und folge dem Aktivierungslink. " +
                s"Danach kannst du dich ",
              <.a(^.href := "/#/login")("anmelden"),
              "."
            )
          )
        } else {

          <.form(
            ^.cls := "gm-form"
          )(
            <.h1("Registrieren"),
            <.div(
              <.input(
                ^.cls := "gm-form",
                ^.width := "99%",
                ^.placeholder := "your@email.com",
                ^.`type` := "email",
                ^.onChange ==> ((e: ReactEventI) =>
                                  $.modState(_emailInput.set(e.target.value))),
                ^.onBlur ==> ((e: ReactEventI) =>
                                $.modState(
                                  _emailError.set(
                                    EmailAddress
                                      .fromString(e.target.value).cata(_ => Maybe.empty,
                                      Maybe.just("Ungültige Email-Adresse.")
                                    )
                                  )
                                ))
              ),
              state.emailError.cata(
                errMsg =>
                  <.div(^.cls := "login-error-info animated shake")(errMsg),
                <.div(^.cls := "login-error-info", ^.visibility := "hidden")(
                  "Fehlerfrei"
                )
              ),
              <.input(
                ^.cls := "gm-form",
                ^.placeholder := "password",
                ^.width := "99%",
                ^.`type` := "password",
                ^.onChange ==> ((e: ReactEventI) => {
                                  val text = e.target.value
                                  $.modState(
                                    _passwordInput.set(text)
                                      andThen _passwordInvalid.modify(
                                        inv =>
                                          if (inv)
                                            validatePassword(text).isEmpty
                                          else inv
                                      )
                                  )
                                }),
                ^.onBlur ==> ((e: ReactEventI) =>
                                $.modState(
                                  _passwordInvalid.set(
                                    validatePassword(e.target.value).isEmpty
                                  )
                                ))
              )
            ),
            if (state.passwordInvalid)
              <.div(^.cls := "login-error-info animated shake")(
                "Muss mindestens 8 Zeichen haben."
              )
            else
              <.div(^.cls := "login-error-info", ^.visibility := "hidden")(
                "Fehlerfrei"
              ),
            <.button(
              ^.cls := "gm-form",
              ^.width := "99%",
              ^.onClick ==> { (e: ReactEventI) =>
                e.preventDefaultCB >>
                  $.modState(_loading.set(true)) >> {
                  val maybeEmail    = EmailAddress.fromString(state.emailInput)
                  val maybePassword = validatePassword(state.passwordInput)
                  val maybeInfo = for {
                    mail <- maybeEmail
                    pw   <- maybePassword
                    _   = println("encrypting")
                    enc = BCrypt.encrypt(pw)
                    _   = println("encrypted")
                  } yield NewUserInfo(mail, enc)
                  maybeInfo.cata(
                    info =>
                      GMClient.post[NewUserInfo, Unit](info) >>=| {
                        case \/-(good) =>
                          $.modState(_sendingEmail.set(true)) >>
                            Callback.future {
                              println("Promising")
                              val p = Promise[Callback]()
                              setTimeout(3000) { () =>
                                println("Successing")
                                p.success(callback)
                              }
                              p.future.map {
                                case cb =>
                                  println("Futuring")
                                  cb
                              }
                            }
                        case -\/(Conflict) =>
                          $.modState(
                            _emailError.set(
                              Maybe.just("E-Mail schon registriert.")
                            ) andThen _loading.set(false)
                          )
                    },
                    $.modState(_passwordInvalid.set(maybePassword.isJust))
                  )
                }
              }
            )(
              <.div(^.cls := (if ($.state.loading) "animated zoomIn" else ""))(
                if ($.state.loading) Animation.loadingSpinner
                else "Frisier mich!"
              )
            )
          )
        }
      }
      .build
}
