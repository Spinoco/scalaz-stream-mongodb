package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject

/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 5:57 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
 

trait PipelineOperator  {

  val asDBObject : DBObject  

  def pipeThrough(other: PipelineOperator): PipelineOperator = {
    (this, other) match {
      case (CombinedPipeline(p1), CombinedPipeline(p2)) => CombinedPipeline(p1 ++ p2)
      case (p1, CombinedPipeline(p2)) => CombinedPipeline(p1 +: p2)
      case (CombinedPipeline(p1), p2) => CombinedPipeline(p1 :+ p2)
      case (p1, p2) => CombinedPipeline(Seq(p1, p2))
    }
  }

  def |>>(other: PipelineOperator): PipelineOperator = pipeThrough(other)

}
