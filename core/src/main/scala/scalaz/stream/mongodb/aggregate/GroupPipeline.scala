package scalaz.stream.mongodb.aggregate

import com.mongodb.{BasicDBObject, DBObject}
import scalaz.stream.mongodb.bson.BSONSerializable


case class GroupId(id: DBObject) {
  def compute(field:ComputedField,  fields: ComputedField*): GroupPipeline = GroupPipeline(id, field +: fields)
}

object GroupId {
  def apply[A:BSONSerializable](p1:(String,A), p2:(String,A)*) : GroupId = {
    val o = new BasicDBObject()
    o.append(p1._1,p1._2)
    p2.foreach(p=>o.append(p._1,p._2))
    GroupId(o)
  }
  
}

/**
 * wraps [[http://docs.mongodb.org/manual/reference/aggregation/group/#pipe._S_group]]
 * @param id        Identity (`_id`) document for $group operation
 * @param computed  computed fields ($sum, $avg ...)
 */
case class GroupPipeline (id: DBObject
                         , computed: Seq[ComputedField]) extends PipelineOperator {
  lazy val asDBObject: DBObject = {
    val o = new BasicDBObject()
    o.append("_id", id)
    computed.foreach {
      field =>
        o.append(field.name,  new BasicDBObject().append(field.operation, field.value))
    }

    new BasicDBObject().append("$group", o)
  }


}




 