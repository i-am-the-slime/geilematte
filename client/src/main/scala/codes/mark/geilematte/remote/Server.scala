package codes.mark.geilematte.remote

import org.scalajs.dom.ext.{Ajax, AjaxException}
import simulacrum.typeclass
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom.XMLHttpRequest
import scodec.Codec
import scodec.bits.ByteVector
import scodec.bits.BitVector
import scodec.codecs.implicits._
import shapeless.Lazy

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.scalajs.js.URIUtils
import scalaz.\/
import scalaz.syntax.either._

sealed trait RemoteError {
  def logAlert = Callback.log(this.toString) >> Callback.alert(this.toString)
}
final case object Conflict           extends RemoteError
final case object Forbidden          extends RemoteError
final case object NotFound           extends RemoteError
final case object PreconditionFailed extends RemoteError

object RemoteError {
  def fromStatus(code: Int): RemoteError = code match {
    case 403 => Forbidden
    case 404 => NotFound
    case 409 => Conflict
    case 412 => PreconditionFailed
  }
}

object GMClient {

  trait Implicits {
    implicit class HideFuture[A](cb: CallbackTo[Future[A]]) {
      def >>=~[B](f: A => CallbackTo[B]): CallbackTo[Future[B]] = {
        cb.flatMap(fut => CallbackTo.future(fut.map(f)))
      }

      def >>=#[B](f: A => Future[CallbackTo[B]]): CallbackTo[Future[B]] = {
        cb.flatMap((futA: Future[A]) => {
          CallbackTo.future(futA.flatMap((a: A) => { f(a) }))
        })
      }

      def >>=|[B](f: A => Callback): Callback = {
        cb.flatMap(fut => Callback.future(fut.map(f)))
      }
    }

    implicit def cbToFutureUnit2cb(futCB: CallbackTo[Future[Unit]]): Callback = {
      futCB >> Callback.empty
    }

    implicit class RemoteErrorOr[A](a: CallbackTo[Future[RemoteError \/ A]]) {
      def logErrors(f: A => Callback): Callback = {
        a.flatMap(
          future =>
            Callback
              .future(future.map(_.fold((re: RemoteError) => re.logAlert, f)))
        )
      }
    }
  }

  def fromBase64[A](base64String: String)(implicit codec: Lazy[Codec[A]]): A =
    Codec.decode(BitVector.fromBase64(base64String).get).require.value

  def get[A](implicit gettable: Gettable[A],
             codec: Lazy[Codec[A]]): CallbackTo[Future[A]] = {

    Callback.log(s"Getting from ${gettable.url}") >>
    CallbackTo.future[A](
      Ajax
        .get(URIUtils.encodeURI(gettable.url), headers = gettable.headers)
        .map(
          xmlHttpRequest =>
            CallbackTo[A](fromBase64[A](xmlHttpRequest.responseText))
        )
    )
  }

  def post[A, B](a: A)(
      implicit postable: Postable[A],
      codec: Lazy[Codec[B]]
  ): CallbackTo[Future[RemoteError \/ B]] = {
    Callback.log(s"Posting $a to ${postable.url}") >>
    CallbackTo.future[RemoteError \/ B](
      Ajax
        .post(
          URIUtils.encodeURI(postable.url),
          postable.toPayload(a),
          headers = postable.headers
        )
        .recover { case ex: AjaxException => ex.xhr }
        .map((xmlHttpRequest: XMLHttpRequest) => {
          val status = xmlHttpRequest.status
          if (status == 200) {
            CallbackTo[RemoteError \/ B](
              fromBase64[B](xmlHttpRequest.responseText).right[RemoteError]
            )
          } else
            CallbackTo[RemoteError \/ B](
              RemoteError.fromStatus(status).left[B]
            )
        })
    )
  }
}

@typeclass
trait Gettable[A] {
  def url: String
  def headers: Map[String, String] = Map.empty
}

@typeclass
trait Postable[A] {
  def url: String
  def toPayload(a: A): Ajax.InputData
  def headers: Map[String, String] =
    Map("Content-Type" -> "application/base64")
}
