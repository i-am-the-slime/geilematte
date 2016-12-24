package codes.mark.geilematte.config

import monocle.macros.Lenses
import org.http4s.Uri

final case class ThisServersUri(uri:Uri)
final case class HmacSecret(secret:String)
@Lenses("_") final case class GMConfig(thisServersUri: ThisServersUri, hmacSecret:HmacSecret)
