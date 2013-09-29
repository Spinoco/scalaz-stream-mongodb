package scalaz.stream.mongodb.aggregate

import org.specs2.Specification
import org.specs2.specification.Snippets

import scalaz.stream.mongodb.collectionSyntax._
import com.mongodb.{DBObject, DBCollection}
import scalaz.stream.mongodb.channel.ChannelResult
import org.specs2.matcher.MatchResult
import scalaz.stream.mongodb.MongoRuntimeSpecification
import org.bson.types.ObjectId
import scala.language.reflectiveCalls
import collection.JavaConverters._

/**
 *
 * User: pach
 * Date: 9/28/13
 * Time: 4:31 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
class MapReduceSpec extends Specification with Snippets with MongoRuntimeSpecification {

  lazy val mapJavaScript: String = ???

  lazy val reduceJavaScript: String = ???

  lazy val finalizeJavaScript: String = ???

  lazy val scopeJavascript: String = ???

  lazy val dbCollection: DBCollection = ???

  def is =
    s2"""

${"MapReduce aggregation".title}      
      
Mongo Streams supports mongodb`s mapreduce functionality. Similarly with Aggregation Pipeline syntax mapReduce is based
on initial query. It also honors the `sort` syntax from query. Following are available syntax for mapReduce operation:

* simple map and reduce operation ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript) }}                             ${simple.inline}
* with finalization function  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript finalize finalizeJavaScript) }}     ${fin.basic}
* with sorting of documents first   ${ snippet { query() sort ("key" Ascending) mapReduce (mapJavaScript reduce reduceJavaScript ) }}      
* with configuring javascript scope   ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript scope scopeJavascript) }}                      
* with configuring in-memory javascript object passing  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript jsModeInMemory) }}           
* with configuring increased verbosity  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript verbose) }}            
* with specifying maximum number of results returned ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript limit (100)) }}


### Specifying output of MapReduce command

Each mapReduce command may eventually specify if results will be stored persistently into database, where these results will be stored and 
how the results will be stored. By default results override any results found in target collection. This is controlled with persists syntax:


* in current database under given collection : ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist ("results")) }}      ${simple.resultCollection}
* in supplied collection and database :    ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist ("resultdb.results")) }}   ${simple.resultCollection2}
* in supplied collection  :    ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist (dbCollection)) }}         
* to merge with current records in result collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist (dbCollection) mergeCurrent) }}   
* to reduce with current records in result collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist (dbCollection) reduceCurrent) }}   
* to store results in shard collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist (dbCollection) asShard) }} 
* to make the store operation nonatomic :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist (dbCollection) nonAtomic) }}   

     """
 
  
  val documents = Seq[DBObject](
    BSONObject("_id" -> new ObjectId, "key" -> "A", "v1" -> "1")
    , BSONObject("_id" -> new ObjectId, "key" -> "A", "v1" -> "2")
    , BSONObject("_id" -> new ObjectId, "key" -> "B", "v1" -> "3")
    , BSONObject("_id" -> new ObjectId, "key" -> "B", "v1" -> "4")
    , BSONObject("_id" -> new ObjectId, "key" -> "C", "v1" -> "5")
    , BSONObject("_id" -> new ObjectId, "key" -> "C", "v1" -> "6")
  )

  val defalutMapF =
    """function() {
      |  emit(this.key, parseInt(this.v1) ); 
      |}
    """.stripMargin
  
  val defaultReduceF =
    """ function(key,values) {
      |   return Array.sum(values);
      |} 
    """.stripMargin

  def simple = new {

    val mrCommand = (defalutMapF reduce defaultReduceF)
    
    val expectedResult =  Seq[DBObject](
      BSONObject("_id" -> "A", "value" -> 3.0d)
      , BSONObject("_id" -> "B", "value" -> 7.0d)
      , BSONObject("_id" -> "C", "value" -> 11.0d))

    def inline = MapReduce(query() mapReduce mrCommand).verify {
      (result, coll) =>
        (result must haveTheSameElementsAs(expectedResult))
    }
    
    
    def resultCollection = MapReduce(query() mapReduce (mrCommand persist("results"))).verify {
      (result, coll) =>
        (result must haveTheSameElementsAs(expectedResult)) and    
          (coll.getDB.getCollection("results").find().iterator().asScala.toSeq must haveTheSameElementsAs(expectedResult))
    }
    
    val resultsDbName = new ObjectId().toString

    def resultCollection2 = MapReduce(query() mapReduce (mrCommand persist(resultsDbName + ".results"))).verify {
      (result, coll) =>
        (result must haveTheSameElementsAs(expectedResult)) and
          (coll.getDB.getMongo().getDB(resultsDbName).getCollection("results").find().iterator().asScala.toSeq must haveTheSameElementsAs(expectedResult))
    }


  }
  
  def fin = new {
    
       def basic = MapReduce(query() mapReduce (defalutMapF reduce defaultReduceF finalize "function(k,v) { return (v*2);}")).verify {
           (result, coll) =>
            result must haveTheSameElementsAs(Seq[DBObject](
              BSONObject("_id" -> "A", "value" -> 6.0d)
              , BSONObject("_id" -> "B", "value" -> 14.0d)
              , BSONObject("_id" -> "C", "value" -> 22.0d))) 
             
           
         }
    
  }
  
 


  case class MapReduce(ch: ChannelResult[DBCollection, DBObject]) {

    val mongo = new WithMongoCollection {}

    def verify(f: (Seq[DBObject], DBCollection) => MatchResult[Any]) = {
      documents.foreach(mongo.collection.insert(_))
      f((mongo.collection through ch).collect.run.toList, mongo.collection)
    }

  }


}
