package codes.mark.geilematte

import scalaz.{Applicative, Cofree, Functor, Monad}
import monocle.macros.{GenLens, Lenses}
import scodec.Codec
import scodec.bits._

final case class Fix[F[_]](unfix: F[Fix[F]])

final case class LatexText(raw: String) extends AnyVal

final case class Question4Id(value: Int) extends AnyVal

final case class AnswerId(value: Int) extends AnyVal

//sealed trait IdAble[A] {
//  def fold[B](withId: Int => A => B, withoutId: A => B): B =
//    this match {
//      case WithId(id, a) => withId(id)(a)
//      case WithoutId(a)  => withoutId(a)
//    }
//}
//final case class WithId[A](id: Int, a: A) extends IdAble[A]
//final case class WithoutId[A](a: A)       extends IdAble[A]

//object IdAble {
//  implicit val monadInst: Monad[IdAble] = new Monad[IdAble] {
//    override def point[A](a: => A): IdAble[A] = WithoutId(a)
//
//    override def bind[A, B](fa: IdAble[A])(f: (A) => IdAble[B]): IdAble[B] =
//      fa match {
//        case WithId(id, a1) => f(a1) match {
//          case WithoutId(a2) => WithId(id, a2)
//          case newId => newId
//        }
//        case WithoutId(a1) => f(a1)
//      }
//  }
//}

@Lenses("_") final case class IdAns(id: Int, answerText: LatexText)

@Lenses("_") final case class Ans(answerText: LatexText)

@Lenses("_") final case class IdQ4(id: Int,
                                   difficulty: Int,
                                   category: Category,
                                   questionText: LatexText,
                                   answer1: IdAns,
                                   answer2: IdAns,
                                   answer3: IdAns,
                                   answer4: IdAns)

@Lenses("_") final case class Q4(difficulty: Int,
                                 category: Category,
                                 questionText: LatexText,
                                 correctAnswer: Ans,
                                 wrongAnswer1: Ans,
                                 wrongAnswer2: Ans,
                                 wrongAnswer3: Ans)

final case class Question4F[A](
                               questionText:LatexText,
                               answer1:A,
                               answer2:A,
                               answer3:A,
                               answer4:A
                     )

object Question4 {
  type AnnotatedQ4 = Cofree[Question4F, Int]
  type FixQ4 = Fix[Question4F]
}
