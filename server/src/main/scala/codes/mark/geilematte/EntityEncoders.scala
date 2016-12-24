package codes.mark.geilematte

import org.http4s._
import org.http4s.headers.{Accept, `Content-Type`}
import scodec.{Codec, DecodeResult}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs.implicits._
import org.http4s.{DecodeResult => DecRes}

import scalaz.concurrent.Task

trait EntityEncoders {

  def b64T[A:Codec]:EntityEncoder[Task[A]] =
    EntityEncoder.simple(`Content-Type`(MediaType.`application/base64`))(
      (a:Task[A]) => ByteVector(Codec.encode(a.unsafePerformSync).require.toBase64.getBytes)
    )

  def b64[A:Codec]:EntityEncoder[A] =
    EntityEncoder.simple(`Content-Type`(MediaType.`application/base64`))(
      (a:A) => ByteVector(Codec.encode(a).require.toBase64.getBytes)
    )
}

trait EntityDecoders {
  def fromB64[A:Codec]:EntityDecoder[A] =
    new EntityDecoder[A] {
      override def consumes = Set(MediaType.`application/base64`)

      override def decode(msg: Message, strict: Boolean) =
        DecRes.success(
        msg.as[String]
          .map(s => Codec.decode[A](BitVector.fromBase64(s).get).require)
          .unsafePerformSync.value
        )
    }
}
