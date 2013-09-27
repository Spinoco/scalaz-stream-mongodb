package scalaz.stream.mongodb.aggregate

/** Sets given field to proposed value */
case class SetField[A](key:String, value:A) extends ProjectPipelineAction
