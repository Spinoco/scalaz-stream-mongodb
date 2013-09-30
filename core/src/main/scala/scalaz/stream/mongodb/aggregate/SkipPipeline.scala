package scalaz.stream.mongodb.aggregate

import com.mongodb.{BasicDBObject, DBObject}

/** skip command in pipeline **/ 
case class SkipPipeline (count:Int) extends PipelineOperator  {
  lazy val asDBObject : DBObject = {
   new BasicDBObject().append("$skip", count)
  }
}
