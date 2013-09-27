package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject
import scala.language.postfixOps

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.bson.BSONSerializable
import scalaz.stream.mongodb.query.{BasicQuery, QueryPair, OrderPair}

/** syntax applied for aggregation commands */
trait AggregationSyntax {

  implicit class AggregationKeySyntax(val self: String) extends AggregationStringOps


  /**
   * Creates the group pipeline operation 
   */
  def group[A: BSONSerializable](p1: (String, A), p2: (String, A)*): GroupId = GroupId(???)

  /**
   * Creates the group pipeline operation 
   */
  def group(o: DBObject): GroupId = GroupId(o)

  /** syntax for project pipeline **/
  def project(actions: ProjectPipelineAction*): ProjectPipeline = ProjectPipeline(actions)

  /** pipeline to filter documents **/
  def only[A](q: QueryPair[A]*): MatchPipeline = MatchPipeline(BasicQuery(q: _*))

  /** pipeline to filter documents **/
  def only(bq: BasicQuery): MatchPipeline = MatchPipeline(bq)

  /** pipeline to filter documents **/
  def only(o: DBObject): MatchPipeline = MatchPipeline(BasicQuery(o))

  /** syntax fro limit pipeline **/
  def limit(count: Int): LimitPipeline = LimitPipeline(count)

  /** syntax fro limit pipeline **/
  def skip(count: Int): SkipPipeline = SkipPipeline(count)

  /** unwinds the element in projection to turn BSON array into separate documents **/
  def unwind(field: String): UnWindPipeline = UnWindPipeline(field)

  /** syntax for creating sort pipeline operation **/
  def sort(h: OrderPair, t: OrderPair*): SortPipeline = SortPipeline(h +: t)


}


object o {


  query() pipeThrough (group("foo" -> "$foo") compute ("sss" push "$foo"))

  query() pipeThrough (project(
    "this" include
    , "that" exclude
    , "ffff" max "$goosh"
    , "goosh" setTo "poosh"
    , "foosh" setTo BSONObject("aha" -> "yes")))

  query() pipeThrough (limit(3))

  query() pipeThrough (skip(3))

  query() pipeThrough (unwind("foo"))

  query() pipeThrough (sort("key" Descending))

  query() |>>
    ((group("foo" -> "$foo") compute ("sss" push "$foo")) |>>
      project("this" include, "that" exclude) |>>
      limit(3) |>>
      skip(1) |>>
      unwind("foo") |>>
      sort("key" Descending) |>>
      only("foo" -> "boo"))


  query() mapReduce ("jsCode" reduce "another" finalize "last")


  query("key" -> "value") |>> (group("grouped" -> "$key1") compute ("sumKey" sum "$key3", "avgKey" avg "$key4"))
  
}



