package codes.mark.geilematte

sealed trait Difficulty

object Difficulty {
  case object Obvious extends Difficulty
  case object Easy    extends Difficulty
  case object Medium  extends Difficulty
  case object Hard    extends Difficulty
  case object Tough   extends Difficulty
  case object Boss    extends Difficulty
}
