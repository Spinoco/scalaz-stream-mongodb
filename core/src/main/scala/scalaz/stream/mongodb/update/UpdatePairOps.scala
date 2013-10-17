package scalaz.stream.mongodb.update

import scalaz.syntax.Ops
import scalaz.stream.mongodb.bson.BSONSerializable


trait UpdatePairOps extends Ops[String] {

  /* $inc field */
  def +=[A: Numeric](a: A) = inc(a)

  def inc[A: Numeric](a: A) = ValueUpdatePair("$inc", self, a)

  /* $set field */
  def :=[A: BSONSerializable](a: A) = set(a)

  def set[A: BSONSerializable](a: A) = ValueUpdatePair("$set", self, a)

  /* $set field if a is Some or $unset if a is None*/
  def :=[A: BSONSerializable](a: Some[A]) = set(a.get)
  def :=[A <: Option[Nothing]](a: A) = remove


  /* $unset field */
  def - = remove

  def remove = ValueUpdatePair[String]("$unset", self, "")

  def ~>(n: String) = renameTo(n)

  def renameTo(n: String) = ValueUpdatePair[String]("$rename", self, n)


  def setOnInsert[A: BSONSerializable](a: A) = ValueUpdatePair("$setOnInsert", self, a)

  def ++=[A: BSONSerializable](a: TraversableOnce[A]) = push(a)

  def addToSet[A: BSONSerializable](a: Set[A]) = ArrayAppendPair("$addToSet", self, a)

  def push[A: BSONSerializable](a: TraversableOnce[A]) = ArrayAppendPair("$push", self, a)

  def popLast = ValueUpdatePair[Int]("$pop", self, 1)

  def popFirst = ValueUpdatePair[Int]("$pop", self, -1)

  def --=[A: BSONSerializable](a: TraversableOnce[A]) = pullAll(a)

  def pullAll[A: BSONSerializable](a: TraversableOnce[A]) = ArrayUpdatePair("$pullAll", self, a)

  def &(i: Int) = BitwiseUpdatePair("$bit", self, i, "and")

  def |(i: Int) = BitwiseUpdatePair("$bit", self, i, "or")
}
