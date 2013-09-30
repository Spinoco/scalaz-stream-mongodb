package scalaz.stream.mongodb.aggregate

import com.mongodb.{BasicDBObject, DBObject}

/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 7:36 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
case class UnWindPipeline(field: String) extends PipelineOperator {
  lazy val asDBObject: DBObject = {
    new BasicDBObject().append("$unwind", field)
  }
}
