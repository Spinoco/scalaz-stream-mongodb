package scalaz.stream.mongodb.aggregate

import com.mongodb.BasicDBObject

/**
 *
 * User: pach
 * Date: 9/27/13
 * Time: 7:01 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */

 

case class CombinedPipeline(operators:Seq[PipelineOperator]) extends PipelineOperator {
  lazy val asDBObject = new BasicDBObject() //never called
}
