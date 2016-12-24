package codes.mark.geilematte.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native
object TwinBcrypt extends js.Object {

  def genSalt(length:Int):String = js.native

  def hashSync(input:String, salt:String):String = js.native

}


