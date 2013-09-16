package scalaz.stream.mongodb.query

 
import com.mongodb.{BasicDBObject, DBObject}

import collection.JavaConverters._


/**
 * Simple key-value query to mongo
 */
case class BasicQuery(o: DBObject = new BasicDBObject) {

  /**
   * Normalizes query so in case there are no operands between predicates, `$and` is injected 
   * Returns tuple for each operation. 
   */
  def normalize: (String, Iterable[DBObject]) = o.keySet().asScala.find(_ match {
    case "$and" | "$or" | "$nor" => true
    case _ => false
  }) match {
    case Some(op) => o.get(op) match {
      case l: java.util.List[DBObject@unchecked] => {
        (op, l.asScala)
      }
      case o => sys.error(s"Unexpected content of $op : $o ")
    }

    case None =>
      ("$and", o.keySet().asScala.map(k => new BasicDBObject().append(k, o.get(k))))
  }

}

/**
 * Helper to build Basic query in case there are multiple key conditions. 
 * In such case we must enforce `$and` between predicates
 */
object BasicQuery {
  def apply[A](pairs: QueryPair[A]*): BasicQuery = {
    val o = new BasicDBObject()
    if (pairs.map(_.key).distinct.size != pairs.size) {
      //indicates multiple $and condition, we have to force $and here
      o.append("$and", pairs.map { case qp: QueryPair[_] => qp.append(new BasicDBObject()) }.asJava)
    } else {
      pairs.foreach { case qp: QueryPair[_] => qp.append(o) }
    }
    BasicQuery(o)
  }
}

