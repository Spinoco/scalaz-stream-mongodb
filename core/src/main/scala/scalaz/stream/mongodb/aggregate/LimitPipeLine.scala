package scalaz.stream.mongodb.aggregate

import com.mongodb.{BasicDBObject, DBObject}

/** limits returned results from pipeline **/
case class LimitPipeline (count:Int) extends PipelineOperator    {
  lazy val asDBObject : DBObject =
    new BasicDBObject().append("$limit", count)
}
