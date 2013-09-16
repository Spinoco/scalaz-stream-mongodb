package scalaz.stream.mongodb.query

import com.mongodb.{BasicDBObject, DBObject}

import scala.collection.JavaConverters._

import scala.language.implicitConversions


/** Predicate combinators **/
trait QueryPairOps[A] extends QueryPredicateOps[QueryPair[A]] {

  def and[B](qp2: QueryPair[B]): BasicQuery = wrap("$and", qp2)

  def or[B](qp2: QueryPair[B]): BasicQuery = wrap("$or", qp2)

  def nor[B](qp2: QueryPair[B]): BasicQuery = wrap("$nor", qp2)

  def and(bq: BasicQuery): BasicQuery = wrap3("$and", bq)

  def or(bq: BasicQuery): BasicQuery = wrap3("$or", bq)

  def nor(bq: BasicQuery): BasicQuery = wrap3("$nor", bq)

  private[mongodb] def wrap[B](cmd: String, qp2: QueryPair[B]) =
    BasicQuery(
      new BasicDBObject().append(cmd, Seq(
        self.append(new BasicDBObject()),
        qp2.append(new BasicDBObject())
      ).asJava))


  private[mongodb] def wrap2(cmd: String, o: DBObject) = BasicQuery(
    new BasicDBObject().append(cmd, Seq(self.append(new BasicDBObject()), o))
  )

  private[mongodb] def wrap3(cmd: String, bq: BasicQuery) = bq.normalize match {
    case (`cmd`, conditions) => BasicQuery(
      new BasicDBObject().append(cmd, (Seq(self.append(new BasicDBObject())) ++ conditions).asJava))

    case _ => wrap2(cmd, bq.o)
  }

}


