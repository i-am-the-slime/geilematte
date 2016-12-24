package codes.mark.geilematte

import java.io.File
import java.net.URLClassLoader

import codes.mark.geilematte.Implicits._
import codes.mark.geilematte.config.{GMConfig, HmacSecret, ThisServersUri}
import codes.mark.geilematte.db.Database
import codes.mark.geilematte.html.{GamePage, MainPage}
import codes.mark.geilematte.mail.{MailTransport, templates}
import codes.mark.geilematte.registration.RegistrationLink
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}
import org.postgresql.util.PSQLException
import scodec.codecs.implicits._

import scala.util.Random
import scalaz.syntax.bind._
import scalaz.concurrent.Task

object GeileMatteServer
    extends ServerApp
    with Database.Implicits
    with EntityEncoders
    with EntityDecoders {

  val config = GMConfig(
    ThisServersUri(
      uri("http://localhost:8080")
    ),
    HmacSecret(
      Random.alphanumeric.take(24).toList.mkString
    )
  )

  val helloWorldService = HttpService {

    /* Main UI */
    case GET -> Root =>
      Ok(MainPage.html)

    /* Main UI */
    case GET -> Root / "add_category" =>
      Ok(MainPage.html)

    /* Main UI */
    case GET -> Root / "question_editor" =>
      Ok(MainPage.html)

    /* Test Game UI */
    case GET -> Root / "game" =>
      Ok(GamePage.html)

    /* Static routes */
    case req @ GET -> "public" /: path =>
      StaticFile
        .fromResource(
          "/public" + path,
          Option(req)
        )
        .fold(NotFound(s"Can't find /public$path"))(Task.now)
  }

//  final case class CategoryParam(cat:Category) extends AnyVal
//  object CategoryParam extends QueryParamMatcher[Category]
  val dbService = HttpService {
    case GET -> Root / "categories" =>
      Ok(
        Task.delay(println("Getting categories")) >>
          Database.categories.task.attempt.flatMap(
            _.fold(
              damn =>
                Task.delay(println(s"Damn: $damn")) >> Task.now(
                  Vector.empty[Category]),
              cats => Task.delay(println(s"Wow $cats")) >> Task.now(cats)
            ))
      )(b64T[Vector[Category]])

    case req @ POST -> Root / "categories" =>
      Ok(
        req
          .as[Category](fromB64[Category])
          .flatMap(
            (cat: Category) =>
              Database
                .createCategory(cat)
                .task
                .attempt
                .flatMap(
                  _.fold(
                    damn => Task.delay(println(s"Damn: $damn")) >> Task.now(0),
                    id => Task.delay(println(s"Wow $id")) >> Task.now(id)
                  )))
      )(b64T[Int])

    case req @ POST -> Root / "question4s" =>
      Ok(
        req.as[Q4](fromB64[Q4]) >>=
          ((question: Q4) =>
             Database
               .createQuestion4(question)
               .task
               .attempt
               .flatMap(_.fold(
                 damn => Task.delay(println(s"Damn: $damn")) >> Task.now(null),
                 id => Task.delay(println(s"Wow $id")) >> Task.now(id)
               )))
      )(b64T[IdQ4])

    case req @ GET -> Root / "question4s" => //TODO add query params
      Ok(
        Database
          .getRandomQuestion(Category("Sets"), 1)
          .task
          .map(vec => Random.shuffle(vec).head)
      )(b64T[IdQ4])

    case req @ POST -> Root / "guess" =>
      Ok(
        req.as[Guess](fromB64[Guess]) >>=
          ((guess: Guess) => Database.checkGuess(guess).task))(b64T[Boolean])

    case req @ POST -> Root / "register" =>
      println(s"I should register $req")
      req.as[NewUserInfo](fromB64[NewUserInfo]) >>=
        ((nui: NewUserInfo) => {
           (for {
             link <- Task.delay(
                      RegistrationLink(
                        Random.alphanumeric.take(30).toList.mkString))
             mail = templates.welcome(nui.email, link)(config.thisServersUri)
             user <- Database.addUser(nui, link).task
             _    <- MailTransport.send(mail)
           } yield user).attempt.unsafePerformSync.fold(
             {
               case ex: PSQLException =>
                 if (ex.getServerErrorMessage.getConstraint == "users_email_key")
                   Conflict("Already exists")
                 else InternalServerError(ex.getMessage)
             },
             userId => Ok(userId)(b64[UserId])
           )
         })

    case req @ GET -> Root / "finish_registration" / registrationLink =>
      println(s"I should finish a registration $req")
      val task =
        Database.finishRegistration(RegistrationLink(registrationLink)).task
      val worked = task.unsafePerformSync
      if (worked) {
        MovedPermanently(config.thisServersUri.uri)
      } else {
        NotFound("Das hat leider nicht geklappt.")
      }
  }

  override def server(args: List[String]): Task[Server] = {
    TestStuff.test
    Database.initUsers.task.unsafePerformSync
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService)
      .mountService(dbService, "/api")
      .start
  }

}
