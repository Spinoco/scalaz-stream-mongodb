package scalaz.stream.mongodb.aggregate

import scalaz.stream.mongodb.query.BasicQuery
import com.mongodb.{BasicDBObject, DBObject}

/**
 *
 * User: pach
 * Date: 9/27/13
 * Time: 6:52 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
case class MatchPipeline(q:BasicQuery) extends PipelineOperator {
  lazy val asDBObject : DBObject = {
    new BasicDBObject().append("$match", q.o)
  }
}
