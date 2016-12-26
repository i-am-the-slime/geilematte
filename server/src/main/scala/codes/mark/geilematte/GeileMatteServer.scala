package codes.mark.geilematte

import java.io.File
import java.net.URLClassLoader

import codes.mark.geilematte.Implicits._
import codes.mark.geilematte.config.{GMConfig, HmacSecret, ThisServersUri}
import codes.mark.geilematte.db.Database
import codes.mark.geilematte.html.{GamePage, MainPage}
import codes.mark.geilematte.logging.Logging
import codes.mark.geilematte.mail.{MailTransport, templates}
import codes.mark.geilematte.registration.RegistrationLink
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}
import org.postgresql.util.PSQLException
import scodec.codecs.implicits._
import db.UserManagement.{
  EmailOrPasswordWrong,
  UserDBProblem,
  UserNotConfirmed
}
import doobie.imports.ConnectionIO
import scalaz.syntax.applicative._

import scala.concurrent.duration._
import scalaz.syntax.traverse._
import scala.util.Random
import scalaz.{Maybe, \/}
import scalaz.syntax.bind._
import scalaz.syntax.std.option._
import scalaz.concurrent.Task

object GeileMatteServer
    extends ServerApp
    with Logging
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
                Task.delay(println(s"Damn: $damn")) >> Task
                  .now(Vector.empty[Category]),
              cats => Task.delay(println(s"Wow $cats")) >> Task.now(cats)
            )
          )
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
                  )
              )
          )
      )(b64T[Int])

    case req @ POST -> Root / "question4s" =>
      req.as[NewQuestionPost](fromB64[NewQuestionPost]) >>=
        ((data: NewQuestionPost) =>
           (for {
             loggedIn <- Database.checkSession(data.userId, data.sessionInfo)
             canEdit  <- Database.canEdit(data.userId)
             maybeId <- if(loggedIn && canEdit)
                           Database.createQuestion4(data.q4).map(Maybe.just)
                         else
                           Maybe.empty[IdQ4].pure[ConnectionIO]

           } yield maybeId).task
             .map(
               _.cata(
                 id => Ok(id)(b64[IdQ4]),
                 Forbidden("Fergett it!")
               )
             )
             .unsafePerformSync)

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
          ((guess: Guess) => Database.checkGuess(guess).task)
      )(b64T[Boolean])

    case req @ POST -> Root / "register" =>
      println(s"I should register $req")
      req.as[NewUserInfo](fromB64[NewUserInfo]) >>=
        ((nui: NewUserInfo) => {
           (for {
             link <- Task.delay(
                      RegistrationLink(
                        Random.alphanumeric.take(30).toList.mkString
                      )
                    )
             mail = templates.welcome(nui.email, link)(config.thisServersUri)
             user <- Database.addUser(nui, link).task
             _    <- Task.delay(log.info(s"Sending mail to ${mail.to}"))
             _    <- MailTransport.send(mail)
           } yield user).attempt.unsafePerformSync.fold(
             {
               case ex: PSQLException =>
                 if (ex.getServerErrorMessage.getConstraint == "users_email_key") {
                   log.info(s"User with email: ${nui.email} already exists")
                   Conflict("Already exists")
                 } else {
                   log.error(s"Unexpected problem", ex)
                   InternalServerError(ex.getMessage)
                 }
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

    case req @ GET -> Root / "salt" =>
      println(s"I should get the salt $req")
      req.params
        .get("email")
        .toMaybe
        .map(s => { log.info(s"Email param: $s"); s })
        .flatMap(EmailAddress.fromString)
        .cata(
          addr => {
            Database
              .getSaltForUser(addr)
              .task
              .unsafePerformSync
              .cata(
                salt => Ok(salt)(b64[Salt]),
                NotFound(s"No such user ${addr}")
              )
          },
          BadRequest("Invalid email in request params")
        )

    case req @ POST -> Root / "login" =>
      println(s"I should log in now $req")
      req.as[LoginAttempt](fromB64[LoginAttempt]) >>=
        (attempt => {
           val dbIO: ConnectionIO[UserDBProblem \/ (UserId, SessionInfo)] =
             for {
               maybeUserId <- Database.checkUserPassword(
                               attempt.email,
                               attempt.passwordWithSalt.password
                             )
               maybeRemember <- maybeUserId.traverse(
                                 (uid: UserId) =>
                                   Database
                                     .rememberUserLogin(uid)(
                                       config.hmacSecret
                                   )
                               )
             } yield
               for {
                 uid <- maybeUserId
                 rem <- maybeRemember
               } yield (uid, rem)
           dbIO.task.unsafePerformSync.fold(
             {
               case UserNotConfirmed =>
                 PreconditionFailed("User not confirmed")
               case EmailOrPasswordWrong =>
                 NotFound("Username or Password wrong")
             },
             (info: (UserId, SessionInfo)) =>
               Ok(info)(b64[(UserId, SessionInfo)])
           )
         })

    case req @ POST -> Root / "check_session" =>
      println(s"I should check the session")
      req.as[SessionCheck](fromB64[SessionCheck]) >>=
        (check => {
           Ok(
             Database
               .checkSession(check.uid, check.session)
               .task
               .unsafePerformSync
           )(b64[Boolean])
         })

    case req @ POST -> Root / "can_edit" =>
      println(s"I should check if a user is allowed to edit")
      req.as[UserId](fromB64[UserId]) >>=
        (uid => {
           Ok(Database.canEdit(uid).task.unsafePerformSync)(b64[Boolean])
         })
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
