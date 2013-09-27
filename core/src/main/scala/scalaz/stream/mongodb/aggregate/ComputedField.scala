package scalaz.stream.mongodb.aggregate


/** Represents field that has to be computed **/
case class ComputedField(name: String, value: String, operation: String) extends ProjectPipelineAction
