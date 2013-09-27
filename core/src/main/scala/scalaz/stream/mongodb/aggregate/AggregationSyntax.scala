package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject
import scala.language.postfixOps

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.bson.BSONSerializable
import scalaz.stream.mongodb.query.OrderPair

/** syntax applied for aggregation commands */
trait AggregationSyntax {

  implicit class AggregationKeySyntax(val self: String) extends AggregationStringOps


  /**
   * Creates the group pipeline operation 
   */
  def group[A: BSONSerializable](p1: (String, A), p2: (String, A)*): GroupPipeline = GroupPipeline(???, Nil)

  /**
   * Creates the group pipeline operation 
   */
  def group(o: DBObject): GroupPipeline = GroupPipeline(o, Nil)

  /** syntax for project pipeline **/
  def project(actions: ProjectPipelineAction*): ProjectPipeline = ProjectPipeline(actions)


  /** syntax fro limit pipeline **/
  def limit(count: Int): LimitPipeline = LimitPipeline(count)

  /** syntax fro limit pipeline **/
  def skip(count: Int): SkipPipeline = SkipPipeline(count)

  /** unwinds the element in projection to turn BSON array into separate documents **/
  def unwind(field: String): UnWindPipeline = UnWindPipeline(field)

  /** syntax for creating sort pipeline operation **/
  def sort(h: OrderPair, t: OrderPair*): SortPipeline =  SortPipeline( h +: t)

  
  
}


object o {


  query() and (group("foo" -> "$foo") and ("sss" push "$foo"))

  query() and (project(
    "this" include
    , "that" exclude
    , "ffff" max "$goosh"
    , "goosh" setTo "poosh"
    , "foosh" setTo BSONObject("aha" -> "yes")))

  query() and (limit(3))

  query() and (skip(3))

  query() and (unwind("foo"))

  query() and (sort( "key" Descending))

  query() and(
    group("foo" -> "$foo") and ("sss" push "$foo")
    , project("this" include, "that" exclude)
    , limit(3)
    , skip(1)
    , unwind("foo")
    , sort( "key" Descending))


  query() mapReduce ("jsCode" reduce "another" finalize "last")


}



