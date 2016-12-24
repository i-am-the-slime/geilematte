package codes.mark.geilematte.mail

import codes.mark.geilematte.EmailAddress

import scalatags.Text.TypedTag

final case class Email(to: EmailAddress,
                       subject: String,
                       content: TypedTag[String])
