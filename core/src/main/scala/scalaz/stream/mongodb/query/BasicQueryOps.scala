package scalaz.stream.mongodb.query

import com.mongodb.BasicDBObject

import scala.collection.JavaConverters._


/** Syntax for building basic queries **/
trait BasicQueryOps extends QueryPredicateOps[BasicQuery] {

  def and[B](qp: QueryPair[B]): BasicQuery = wrap("$and", qp)

  def or[B](qp: QueryPair[B]): BasicQuery = wrap("$or", qp)

  def nor[B](qp: QueryPair[B]): BasicQuery = wrap("$nor", qp)

  def and(bq2: BasicQuery): BasicQuery = wrap2("$and", bq2)

  def or(bq2: BasicQuery): BasicQuery = wrap2("$or", bq2)

  def nor(bq2: BasicQuery): BasicQuery = wrap2("$nor", bq2)


  private[mongodb] def wrap[B](cmd: String, qp: QueryPair[B]) = self.normalize match {
    case (`cmd`, conditions) => BasicQuery(
      new BasicDBObject().append(cmd, (conditions ++ Seq(qp.append(new BasicDBObject()))).asJava))

    case _ => BasicQuery(
      new BasicDBObject().append(cmd, Seq(self.o, qp.append(new BasicDBObject())))
    )
  }

  private[mongodb] def wrap2[B](cmd: String, bq2: BasicQuery) = (self.normalize, bq2.normalize) match {
    case ((`cmd`, conds1), (`cmd`, conds2)) => BasicQuery(
      new BasicDBObject().append(cmd, (conds1 ++ conds2).asJava))
    case _ => BasicQuery(
      new BasicDBObject().append(cmd, Seq(self.o, bq2.o).asJava)
    )
  }


}
