package scalaz.stream.mongodb.aggregate

import com.mongodb.{BasicDBObject, DBObject}
import scalaz.stream.mongodb.query.SimpleProjection


/**
 * Wraps  [[http://docs.mongodb.org/manual/reference/aggregation/project/#pipe._S_project]]
 * @param action Actions to perform in the project stage
 */
case class ProjectPipeline(action: Seq[ProjectPipelineAction]) extends PipelineOperator {
  lazy val asDBObject: DBObject = {
    val o = new BasicDBObject()

    action.foreach {
      case SetField(k, v) => o.append(k, v) 
      case SimpleProjection("_id", true) => o.append("_id", 0)
      case SimpleProjection(k, true) => sys.error("only \"_id\" can be excluded for now")
      case SimpleProjection(k, false) => o.append(k, 1)
    } 

    new BasicDBObject("$project", o)
  }
}


trait ProjectPipelineAction


