package codes.mark.geilematte.mail

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail._
import javax.mail.event.{ConnectionEvent, ConnectionListener}

import scalaz.concurrent.Task
import scalaz._
import Scalaz._
import scala.concurrent.duration._

object MailTransport {
  val username = "geilematte@gmail.com"
  val password = "Ru3-YE7-P7J-DcR"

  val sendSession = Task.delay {
    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", "587")
    Session.getInstance(props, new Authenticator() {
      override def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(username, password)
    })
  }

  def send(email: Email): Task[Unit] = {
    sendSession.map { session =>
      val message = new MimeMessage(session)
      message setFrom new InternetAddress(username)
      message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(email.to.toString).map(x => x: Address))
      message setSubject email.subject
      message setContent(email.content.render, "text/html; charset=utf-8")
      Transport send message
    }
  }

//  val receiveSession = {
//    val props = System.getProperties
//    props.setProperty("mail.store.protocol", "imaps")
//    Session.getDefaultInstance(props, null)
//  }

//  def listener(inbox:Folder) = new ConnectionListener {
//    override def disconnected(e: ConnectionEvent): Unit =
//      println("disconnected")
//
//    override def opened(e: ConnectionEvent): Unit =  {
//      println("opened")
//      val messages = inbox.getMessages()
//      for {
//        msg     <- messages
//        subject  = msg.getSubject
//      } println(subject)
//    }
//
//    override def closed(e: ConnectionEvent): Unit = println("closed")
//  }

//  def check: Disjunction[Throwable, Unit] = {
//    val store = receiveSession.getStore("imaps")
//    store.connect("imap.gmail.com", username, password)
//    val inbox = store.getFolder("INBOX")
//    inbox.addConnectionListener(listener(inbox))
//    inbox.open(Folder.READ_ONLY)
//    Task.schedule((), 20 seconds)
//  }

}
