package scalaz.stream.mongodb.aggregate


/**
 * Wraps  [[http://docs.mongodb.org/manual/reference/aggregation/project/#pipe._S_project]]
 * @param action Actions to perform in the project stage
 */
case class ProjectPipeline(action: Seq[ProjectPipelineAction]) extends PipelineOperator  {
  lazy val asDBObject = ???
}


trait ProjectPipelineAction


