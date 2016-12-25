package codes.mark.geilematte

import codes.mark.geilematte.BCrypt
import codes.mark.geilematte.facades.Cookies
import codes.mark.geilematte.remote._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{
  Callback,
  CallbackTo,
  ReactComponentB,
  ReactEventI
}
import monocle.macros.Lenses

import scalaz.Maybe
import scala.concurrent.Future
import scalaz.\/
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

@Lenses("_") final case class LoginState(emailInput: String,
                                         passwordInput: String,
                                         userId: Maybe[UserId],
                                         emailInvalid: Boolean,
                                         rememberLogin: Boolean)

object Login extends Postables with Gettables with GMClient.Implicits {

  def validatePassword(s: String): Maybe[String] = {
    if (s.length >= 8) Maybe.just(s) else Maybe.empty
  }

  import LoginState._
  def component =
    ReactComponentB[(UserId, SessionInfo) => Callback]("Log In Component")
      .initialState(LoginState("", "", Maybe.empty, false, false))
      .renderPS { ($, callback, state) =>
        <.form(
          ^.cls := "gm-form"
        )(
          <.h1("Anmelden"),
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
                                _emailInvalid.set(
                                  EmailAddress
                                    .fromString(e.target.value)
                                    .isEmpty
                                )
                              ))
            ),
            if (state.emailInvalid)
              <.div(^.cls := "login-error-info")("Ungültige Email-Adresse.")
            else EmptyTag,
            <.input(
              ^.cls := "gm-form",
              ^.placeholder := "password",
              ^.width := "99%",
              ^.`type` := "password",
              ^.onChange ==> ((e: ReactEventI) =>
                                $.modState(_passwordInput.set(e.target.value)))
            )
          ),
          <.input(
            ^.`type` := "checkbox",
            ^.id := "rememberLoginCheckBox",
            ^.onChange ==> ((e: ReactEventI) => {
                              val setTo = e.target.value
                              val asBool = setTo match {
                                case "on"  => true
                                case "off" => false
                              }
                              $.modState(
                                _rememberLogin.set(asBool)
                              )
                            })
          ),
          <.label(^.`for` := "rememberLoginCheckBox")(" Passwort merken"),
          <.button(
            ^.cls := "gm-form",
            ^.width := "99%",
            ^.onClick ==> { (e: ReactEventI) =>
              e.preventDefaultCB >> {
                val maybeEmail    = EmailAddress.fromString(state.emailInput)
                val maybePassword = validatePassword(state.passwordInput)
                val maybeInfo = for {
                  mail <- maybeEmail
                  pw   <- maybePassword
                } yield (mail, pw)
                maybeInfo.cata({
                  case (email, password) =>
                    LoginHandler
                      .attempt(state.rememberLogin, email, password, callback)
                }, $.modState(_emailInvalid.set(maybeEmail.isJust)))
              }
            }
          )("Schnipp-Schnapp"),
          <.div(
            <.p(
              "Noch kein Account? Dann schnell ",
              <.a(^.href := "/#/register")("registrieren"),
              "."
            )
          )
        )
      }
      .componentWillMount(
        cwm => {
          LoginHandler.fromCookies >>=
            (_.cata(cwm.props.tupled, Callback.empty))
        }
      )
      .build
}
