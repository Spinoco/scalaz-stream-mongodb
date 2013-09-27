package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject


case class GroupId(id: DBObject) {
  def compute(field:ComputedField,  fields: ComputedField*): GroupPipeline = GroupPipeline(id, field +: fields)
}

/**
 * wraps [[http://docs.mongodb.org/manual/reference/aggregation/group/#pipe._S_group]]
 * @param id        Identity (`_id`) document for $group operation
 * @param computed  computed fields ($sum, $avg ...)
 */
case class GroupPipeline(id: DBObject
                         , computed: Seq[ComputedField]) extends PipelineOperator {
  lazy val asDBObject = ???


}




 