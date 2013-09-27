package scalaz.stream.mongodb.aggregate

import scalaz.stream.mongodb.query.OrderPair

/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 7:39 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
case class SortPipeline(op: Seq[OrderPair]) extends PipelineOperator {
  lazy val asDBObject = ???
}
