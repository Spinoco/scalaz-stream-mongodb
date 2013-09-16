package scalaz.stream.mongodb.query

 

import scalaz.stream.mongodb.bson._

import scala.collection.JavaConverters._
import scalaz.syntax.Ops
import com.mongodb.BasicDBObject


trait QueryPairBuilderOps extends Ops[String] { 

  def ===[A: QueryPredicateWitness](a: A): QueryPair[A] = BasicQueryPair(self, a)

  def =/=[A: QueryPredicateWitness](a: A): QueryPair[A] = wrap("$ne", a)

  def ≠[A: QueryPredicateWitness](a: A): QueryPair[A] = =/=(a)

  private def wrap[A](s: String, a: A): QueryPair[A] = wrap2(s, a, a)

  private def wrap2[A, B](s: String, a: A, b: B): QueryPair[A] = BasicQueryPair(self, a, Some(new BasicDBObject().append(s, b)))

  def >[A: QueryPredicateWitness](a: A): QueryPair[A] = wrap("$gt", a)

  def >=[A: QueryPredicateWitness](a: A): QueryPair[A] = wrap("$gte", a)

  def <[A: QueryPredicateWitness](a: A): QueryPair[A] = wrap("$lt", a)

  def <=[A: QueryPredicateWitness](a: A): QueryPair[A] = wrap("$lte", a)

  def within[A: QueryPredicateWitness](h: A, la: A*): QueryPair[Seq[A]] = wrap2("$in", h +: la, (h +: la).toSeq.asJava)

  def notWithin[A: QueryPredicateWitness](la: A*): QueryPair[Seq[A]] = wrap2("$nin", la, la.toSeq.asJava)

  def all(ks: String*): QueryPair[Seq[String]] = wrap2("$all", ks, ks.toSeq.asJava)

  def present: QueryPair[Boolean] = wrap("$exists", true)

  def %(m: Int, r: Int): QueryPair[(Int, Int)] = wrap2("$mod", (m, r), Seq(m, r).asJava)

  def elementMatch[A](qp: QueryPair[A]*): ElemMatchPair = ElemMatchPair(self, BasicQuery(qp: _*))

  def elementMatch(q: BasicQuery): ElemMatchPair = ElemMatchPair(self, q)

  def ofSize(i: Int): QueryPair[Int] = wrap("$size", i)

  def regex(r: String, options: String = ""): QueryPair[String] =
    if (options.trim.isEmpty) wrap("$regex", r)
    else BasicQueryPair(self, r, Some(new BasicDBObject().append("$regex", r).append("$options", options.trim)))

  def ofType(t: BSONType.Value): QueryPair[BSONType.Value] = wrap2("$type", t, t.id)

}
