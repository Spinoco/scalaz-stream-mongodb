package scalaz.stream.mongodb.aggregate

import com.mongodb.{DBCollection, DBObject}
import scalaz.stream.mongodb.query.Query
import scalaz.stream.mongodb.channel.ChannelResult

import scalaz.stream.Process
import scalaz.concurrent.Task

import collection.JavaConverters._

/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 5:57 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */


trait PipelineOperator {

  val asDBObject: DBObject

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


object PipelineOperator {

  def toChannelResult(q: Query, pipeline: PipelineOperator): ChannelResult[DBCollection, DBObject] = ChannelResult {
    import Task._
    Process.eval[Task, DBCollection => Task[Process[Task, DBObject]]] {
      now {
        c: DBCollection =>
          val result =
            pipeline match {
              case CombinedPipeline(head :: tail) => c.aggregate(head.asDBObject, tail.map(_.asDBObject): _*)
              case other => c.aggregate(other.asDBObject)
            }

          if (result.getCommandResult.ok()) {
            now(Process.emitAll(result.results().asScala.toSeq).evalMap(now(_)))
          } else {
            now(Process.fail(result.getCommandResult.getException))
          }

      }
    }

  }

}
