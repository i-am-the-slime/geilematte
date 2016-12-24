package codes.mark.geilematte

import codes.mark.geilematte.remote.{Conflict, GMClient, Postables}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactEventI}
import monocle.macros.Lenses

import scalaz.{-\/, Maybe, \/, \/-}
import scala.concurrent.Future
import scodec.codecs.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

@Lenses("_") final case class RegisterState(emailInput: String,
                                            passwordInput: String,
                                            emailError: Option[String],
                                            passwordInvalid: Boolean)

object Register extends Postables with GMClient.Implicits {

  def validatePassword(s: String): Option[String] = {
    if (s.length >= 8) Option(s) else None
  }

  import RegisterState._
  def component =
    ReactComponentB[Callback]("Log In Component")
      .initialState(RegisterState("", "", None, false))
      .renderPS { ($, callback, state) =>
        <.form(
          ^.cls := "gm-form"
        )(
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
                                  EmailAddress.fromString(e.target.value) match {
                                    case None => Option("Ungültige Email-Adresse.")
                                    case _ => None
                                  })
                              ))
            ),
            (state.emailError match {
              case None => EmptyTag
              case Some(errMsg) =>
                <.div(^.cls := "login-error-info")(errMsg)
            }),
            <.input(
              ^.cls := "gm-form",
              ^.placeholder := "password",
              ^.width := "99%",
              ^.`type` := "password",
              ^.onChange ==> ((e: ReactEventI) =>
                                $.modState(_passwordInput.set(e.target.value))),
              ^.onBlur ==> ((e: ReactEventI) =>
                              $.modState(
                                _passwordInvalid.set(
                                  validatePassword(e.target.value).isEmpty)
                              ))
            )
          ),
          (if (state.passwordInvalid)
             <.div(^.cls := "login-error-info")(
               "Muss mindestens 8 Zeichen haben.")
           else EmptyTag),
          <.button(
            ^.cls := "gm-form",
            ^.width := "99%",
            ^.onClick ==> {
              (e:ReactEventI) => e.preventDefaultCB >> {
                val maybeEmail    = EmailAddress.fromString(state.emailInput)
                val maybePassword = validatePassword(state.passwordInput)
                val maybeInfo = for {
                  mail <- maybeEmail
                  pw   <- maybePassword
                  _   = println("encrypting")
                  enc = BCrypt.encrypt(pw)
                  _   = println("encrypted")
                } yield NewUserInfo(mail, enc)
                maybeInfo.fold(
                  $.modState(
                    _passwordInvalid
                      .set(maybePassword.isDefined))
                )(
                  info =>
                    GMClient
                      .post[NewUserInfo, Unit](info) >>=| {
                      case \/-(good) => callback
                      case -\/(Conflict) => $.modState(_emailError.set(Option("E-Mail schon registriert.")))
                    }
                )
              }
            }
          )("Frisier mich!")
        )
      }
      .build
}
