package scalaz.stream.mongodb.aggregate

/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 7:30 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */

/** limits returned results from pipeline **/
case class LimitPipeline (count:Int) extends PipelineOperator    {
  lazy val asDBObject = ???
}
