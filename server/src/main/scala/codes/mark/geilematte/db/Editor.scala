package codes.mark.geilematte.db

import codes.mark.geilematte._
import doobie.imports._

trait Editor extends Fights {

  def createCategory(category: Category): ConnectionIO[Int] = {
    val n = category.name
    sql"""
      insert into
      categories (name)
      values ($n)"""
      .update
      .withUniqueGeneratedKeys[Int]("cat_id")
  }

  def insertAnswer(answer: Ans): ConnectionIO[IdAns] = {
    println(s"Inserting answer $answer")
    sql"insert into answers (text) VALUES (${answer.answerText.raw})".update
      .withUniqueGeneratedKeys[Int]("ans_id")
      .map(i => IdAns(i, answer.answerText))
  }

  def createQuestion4(q4: Q4): ConnectionIO[IdQ4] = {
    for {
      a1  <- insertAnswer(q4.correctAnswer)
      a2  <- insertAnswer(q4.wrongAnswer1)
      a3  <- insertAnswer(q4.wrongAnswer2)
      a4  <- insertAnswer(q4.wrongAnswer3)
      cat <- getCategoryId(q4.category)
      _ = println(s"Schnapper: $a1, $a2, $a3, $a4, $cat")
      id <- sql"insert into questions4 (question, difficulty, category, answer, wrong1, wrong2, wrong3) values (${q4.questionText.raw}, ${q4.difficulty}, $cat, ${a1.id}, ${a2.id}, ${a3.id}, ${a4.id})".update
        .withUniqueGeneratedKeys[Int]("q4_id")
    } yield
      IdQ4(id, q4.difficulty, q4.category, q4.questionText, a1, a2, a3, a4)
  }
}
