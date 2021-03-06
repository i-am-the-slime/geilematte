package codes.mark.geilematte.db

import codes.mark.geilematte.{Difficulty, MattePermissions}
import codes.mark.geilematte.Difficulty._
import codes.mark.geilematte.MattePermissions.{
  MatteAdmin,
  MatteEditor,
  MatteUser
}
import doobie.imports.Atom
import doobie.postgres.pgtypes._

trait implicits {

  implicit val DifficultyAtom: Atom[Difficulty] =
    pgEnumString("difficulty", {
      case "obvious" => Obvious
      case "easy"    => Easy
      case "medium"  => Medium
      case "hard"    => Hard
      case "tough"   => Tough
      case "boss"    => Boss
    }, {
      case Obvious => "obvious"
      case Easy    => "easy"
      case Medium  => "medium"
      case Hard    => "hard"
      case Tough   => "tough"
      case Boss    => "boss"
    })

  implicit val PermissionsAtom: Atom[MattePermissions] =
    pgEnumString("permissions", {
      case "user"   => MatteUser
      case "editor" => MatteEditor
      case "admin"  => MatteAdmin
    }, {
      case MatteUser   => "user"
      case MatteEditor => "editor"
      case MatteAdmin  => "admin"
    })
}
