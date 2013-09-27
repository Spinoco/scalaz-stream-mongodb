package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject

/**
 * wraps [[http://docs.mongodb.org/manual/reference/aggregation/group/#pipe._S_group]]
 * @param id        Identity (`_id`) document for $group operation
 * @param computed  computed fields ($sum, $avg ...)
 */
case class GroupPipeline(id: DBObject
                         , computed: Seq[ComputedField]) extends PipelineOperatorAfterQuery {
  def asDBObject = ???

  def and(fields: ComputedField*): GroupPipeline = copy(computed = computed ++ fields)

}




 