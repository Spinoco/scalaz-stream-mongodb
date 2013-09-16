package scalaz.stream.mongodb.query

import com.mongodb.{BasicDBObject, DBObject}
import scala.collection.JavaConverters._

 

sealed trait QueryPair[+A] {
  val key: String

  private[query] def negate: QueryPair[A]

  /**
   * Appends this pair to given object
   */
  def append(to: BasicDBObject): BasicDBObject
}


/**
 * Pair used in projection from the query as well as in query itself
 * @param key      Key that has to be projected or elem-matched
 * @param q        Projection predicate on key predicate on elemMatch
 * @param not      Negates predicate
 */
case class ElemMatchPair(key: String, q: BasicQuery, not: Boolean = false) extends ProjectionPair with QueryPair[BasicQuery] {
  private[query] def negate = copy(not = true)

  def append(to: BasicDBObject): BasicDBObject = {
    to.append(key, new BasicDBObject().append("$elemMatch", q.o))
    to
  }
}


/**
 * Predicate pair that builds query on given key 
 * @param key             Key to apply predicate on
 * @param pattern         Value of the key
 * @param dbo             object predicate on the keyd
 * @param not             negates the predicate when set to true
 * @tparam A              BSONValue constrain
 */
case class BasicQueryPair[+A](key: String, pattern: A, dbo: Option[DBObject] = None, not: Boolean = false) extends QueryPair[A] {


  def append(to: BasicDBObject): BasicDBObject = {
    dbo match {
      case Some(o) if not =>
        to.append(key, new BasicDBObject().append("$not", o))
      case Some(o) =>
        to.append(key, o)
      case None if not =>
        to append(key, new BasicDBObject().append("$ne", pattern))
      case None =>
        to.append(key, pattern)
    }
    to
  }

  private[query] def negate = copy(not = true)


}

sealed trait ProjectionPair {
  val key: String

  def append(to: BasicDBObject): BasicDBObject
}

/**
 * Simple projection pair 
 */
case class SimpleProjection(key: String, exclude: Boolean = false) extends ProjectionPair {
  def append(to: BasicDBObject): BasicDBObject = {
    to.append(key, if (exclude) 0 else 1)
    to
  }
}

/**
 * Allows to project slices of arrays 
 */
case class SliceProjection(key: String, index: Int, size: Option[Int] = None) extends ProjectionPair {

  def append(to: BasicDBObject): BasicDBObject = {
    to.append(key, new BasicDBObject().append("$slice", size.map(s => Seq(index, s).asJava).getOrElse(index)))
    to
  }

}


