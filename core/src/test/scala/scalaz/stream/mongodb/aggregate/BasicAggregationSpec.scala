package scalaz.stream.mongodb.aggregate

import org.specs2.Specification
import org.specs2.specification.Snippets

import scalaz.stream.mongodb.collectionSyntax._

/**
 *
 * User: pach
 * Date: 9/28/13
 * Time: 5:19 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
class BasicAggregationSpec extends Specification with Snippets{
  
  def is =
    s2"""
    
${"Generic aggregation functions"}
       

Apart from map-reduce and aggregation pipeline Mongo Streams supports basic mongodb aggregation functions. Supported
aggregation functions are: 

* count of document matching supplied query ${ snippet { query() count}} 
* getting distinct values for supplied key ${ snippet { query() distinct("key")}}       
        
      
    """

}
