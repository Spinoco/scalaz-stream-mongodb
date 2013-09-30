package scalaz.stream.mongodb.aggregate

import scalaz.stream.mongodb.bson.BSONSerializable


/** Represents field that has to be computed **/
case class ComputedField(name: String, value: String, operation: String) extends ProjectPipelineAction
