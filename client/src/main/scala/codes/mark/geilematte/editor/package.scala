package codes.mark.geilematte

import codes.mark.geilematte.remote.{GMClient, Gettables, Postables}

package object editor extends Gettables with Postables with GMClient.Implicits
