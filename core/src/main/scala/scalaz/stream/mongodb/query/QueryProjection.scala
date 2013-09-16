package scalaz.stream.mongodb.query

import com.mongodb.{BasicDBObject, DBObject}


/**
 * Holds the projection pairs
 * @param qp
 */

case class QueryProjection(qp: Seq[ProjectionPair]) {

  private[mongodb] val asDBObject: DBObject = {
    val builder = new BasicDBObject()
    qp.foreach(p => p.append(builder))
    builder
  }

}