package codes.mark.geilematte.db

import codes.mark.geilematte._
import doobie.imports._

trait Fights {
  val categories: ConnectionIO[Vector[Category]] =
    sql"""
      select name
      from categories"""
      .query[Category]
      .vector

  def getCategoryId(category: Category): ConnectionIO[Int] = {
    sql"""
      select cat_id
      from categories
      where name = ${category.name}"""
      .query[Int]
      .unique
  }

  def getRandomQuestion(category: Category,
                        difficulty: Int): ConnectionIO[Vector[IdQ4]] = {
    sql"""select q4_id, question, difficulty, cats.name,
      as1.ans_id, as1.text,
      as2.ans_id, as2.text,
      as3.ans_id, as3.text,
      as4.ans_id, as4.text
      from questions4
      JOIN categories as cats on cats.cat_id = category
      JOIN answers as as1 on as1.ans_id = answer
      JOIN answers as as2 on as2.ans_id = wrong1
      JOIN answers as as3 on as3.ans_id = wrong2
      JOIN answers as as4 on as4.ans_id = wrong3
      WHERE cats.name = ${category.name} and difficulty <= $difficulty;"""
      .query[(Int,
      String,
      Int,
      String,
      Int,
      String,
      Int,
      String,
      Int,
      String,
      Int,
      String)]
      .vector
      .map(_.map {
        case (q4Id,
        question,
        hardness,
        _,
        correctId,
        correctAnswer,
        wrong1Id,
        wrong1Answer,
        wrong2Id,
        wrong2Answer,
        wrong3Id,
        wrong3Answer) =>
          IdQ4(q4Id,
            hardness,
            category,
            LatexText(question),
            IdAns(correctId, LatexText(correctAnswer)),
            IdAns(wrong1Id, LatexText(wrong1Answer)),
            IdAns(wrong2Id, LatexText(wrong2Answer)),
            IdAns(wrong3Id, LatexText(wrong3Answer)))
      })
  }

  def checkGuess(guess: Guess): ConnectionIO[Boolean] = {
    sql"""select q4_id from questions4
          where q4_id = ${guess.question4Id.value}
          and answer = ${guess.answerId.value}
          limit 1
    """.query[Int].option.map(_.isDefined)
  }

}
