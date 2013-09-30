package scalaz.stream.mongodb.userguide

import org.specs2.Specification
import scalaz.stream.mongodb.aggregate.{PipelineSpec, MapReduceSpec, BasicAggregationSpec}

class AggregationUsageSpec extends Specification{
  def is =
    s2"""
    
${"Aggregation framework".title} 
       
       
Mongo Streams has support for all aggregation framework operations that are supported by mongodb. 
Please take look into following examples to see more:
      
### Simple Aggregation operations 
      
These are simple operations to return count of elements or distinct values. For more please look  ${ "here" ~/ new BasicAggregationSpec() }.


### Map-Reduce aggregation operations

Mongo Streams allows you to create mongo's mapreduce javascript aggregation operations. For syntax and examples look  ${ "here" ~/ new MapReduceSpec() }.
      
      
### Aggregation Pipieline operations
      
Mongo Streams has limitted support for aggregation pipeline commands. All the aggregation pipeline commands are now 
supported, but only basic expressions are supported now for $$project operation. For list and exmaples of supported 
functionality, please look  ${ "here" ~/ new PipelineSpec() }.
      
      
    """
}
