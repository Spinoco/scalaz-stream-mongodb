package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject

/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 5:57 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait PipelineOperator {
  
  
  def asDBObject : DBObject
  
}

trait PipelineOperatorAfterQuery extends PipelineOperator
