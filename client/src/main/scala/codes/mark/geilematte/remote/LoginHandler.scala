package codes.mark.geilematte.remote

import codes.mark.geilematte.facades.Cookies
import codes.mark.geilematte.{
  BCrypt,
  EmailAddress,
  LoginAttempt,
  Salt,
  SessionCheck,
  SessionInfo,
  UserId,
  remote
}
import japgolly.scalajs.react.{Callback, CallbackTo}

import scalaz.Maybe
import scalaz.syntax.std.option._
import scodec.codecs.implicits._
import codes.mark.geilematte.remote.GMClient

import scala.concurrent.Future

object LoginHandler extends Gettables with Postables with GMClient.Implicits {
  def attempt(writeCookies: Boolean,
              email: EmailAddress,
              rawPassword: String,
              handleSuccess: (UserId, SessionInfo) => Callback,
              handleError: RemoteError => Callback = alertErrorHandling) =
    GMClient.get[Salt](saltGettable(email), implicitly) >>=|
      (salt => {
         val enc     = BCrypt.encryptWithSalt(rawPassword, salt)
         val attempt = LoginAttempt(email, enc)
         GMClient.post[LoginAttempt, (UserId, SessionInfo)](attempt) >>=~
           (_.fold(
             handleError,
             (result: (UserId, SessionInfo)) => {
               if (writeCookies)
                 cookies.write(result._1, result._2) >>
                   handleSuccess.tupled(result)
               else
                 handleSuccess.tupled(result)
             }
           ))
       })

  def alertErrorHandling: RemoteError => Callback = {
    case PreconditionFailed =>
      Callback.alert(
        s"Zuerst bitte den Bestätigungslink in deiner E-Mail anklicken."
      )
    case NotFound =>
      Callback.alert(s"E-Mail nicht im System, oder Passwort falsch.")
    case _ =>
      Callback.alert("Bad, too")
  }

  object cookies {
    val UidKey     = "uid"
    val SessionKey = "session"
    def write(uid: UserId, si: SessionInfo): Callback = {
      Callback.log("Writing cookies.") >>
        Callback(
          Cookies.write(
            Map(
              UidKey     -> uid.id.toString,
              SessionKey -> si.unboxed
            )
          )
        )
    }
    def read: CallbackTo[Maybe[(UserId, SessionInfo)]] = {
      CallbackTo {
        val cookies = Cookies.read
        for {
          uid <- cookies
                  .get(UidKey)
                  .toMaybe
                  .flatMap(x => Maybe.fromTryCatchNonFatal(x.toInt))
          session <- cookies.get(SessionKey).toMaybe
        } yield (UserId(uid), SessionInfo(session))
      }
    }

  }

  def fromCookies: CallbackTo[Future[Maybe[(UserId, SessionInfo)]]] = {
    val nope = CallbackTo(Maybe.empty[(UserId, SessionInfo)])
    cookies.read >>= (_.cata(
      (x: (UserId, SessionInfo)) =>
        GMClient.post[SessionCheck, Boolean](
          SessionCheck(x._1, x._2)
        ) >>=~ (
          _.fold(
            _ => nope,
            fine =>
              if (fine) CallbackTo(Maybe.just(x))
              else nope
          )
      ),
      nope.map(Future.successful)
    ))
  }
}
