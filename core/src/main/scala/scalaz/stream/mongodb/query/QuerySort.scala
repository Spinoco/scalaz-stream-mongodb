package scalaz.stream.mongodb.query

import com.mongodb.{BasicDBObject, DBObject}

import collection.JavaConverters._

/**
 * Holder for sort options
 * @param op
 */
case class QuerySort(op: Seq[OrderPair]) {
  lazy val o: DBObject = {
    val bo = new   BasicDBObject()
    bo.putAll(op.map(opr => (opr.k -> opr.o.id)).toMap.asJava)
    bo
  } 
}