package codes.mark.geilematte.remote

import codes.mark.geilematte.{Category, EmailAddress, Guess, IdQ4, LoginAttempt, NewUserInfo, Q4, Salt, UserId}
import org.scalajs.dom.ext.Ajax.InputData
import scodec.Codec
import scodec.codecs.implicits._

trait Gettables {
  implicit val categoryGettable:Gettable[Vector[Category]] = new Gettable[Vector[Category]] {
    override def url = "/api/categories"
  }

  implicit val q4Gettable:Gettable[IdQ4] = new Gettable[IdQ4] {
    override def url = "/api/question4s"
  }

  def saltGettable(mail:EmailAddress):Gettable[Salt] = new Gettable[Salt] {
    override def url = s"/api/salt?email=${mail.toString}"
  }
}

trait Postables {

  implicit val categoryPostable:Postable[Category] = new Postable[Category] {
    override def url = "/api/categories"

    override def toPayload(a: Category): InputData =
      Codec.encode(a).require.toBase64
  }

  implicit val q4Postable:Postable[Q4] = new Postable[Q4] {
    override def url = "api/question4s"

    override def toPayload(a: Q4): InputData =
      Codec.encode(a).require.toBase64
  }

  implicit val guessPostable:Postable[Guess] = new Postable[Guess] {
    override def url = "api/guess"

    override def toPayload(a: Guess): InputData =
      Codec.encode(a).require.toBase64
  }

  implicit val newUserInfoPostable:Postable[NewUserInfo] = new Postable[NewUserInfo] {
    override def url = "api/register"

    override def toPayload(a: NewUserInfo): InputData =
      Codec.encode(a).require.toBase64
  }

  implicit val loginAttemptPostable:Postable[LoginAttempt] = new Postable[LoginAttempt] {
    override def url = "api/login"

    override def toPayload(a:LoginAttempt): InputData =
      Codec.encode(a).require.toBase64
  }
}
