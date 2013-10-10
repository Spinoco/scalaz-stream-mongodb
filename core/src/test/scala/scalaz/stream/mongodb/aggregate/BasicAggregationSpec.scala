package scalaz.stream.mongodb.aggregate

import org.specs2.Specification
import org.specs2.specification.Snippets

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.DBCollection
import org.specs2.matcher.MatchResult
import scalaz.stream.mongodb.MongoRuntimeSpecification
import scalaz.stream.mongodb.query.ReadPreference
import scalaz.stream.mongodb.bson.BSONSerializable


class BasicAggregationSpec extends Specification with Snippets with MongoRuntimeSpecification {

  def is =
    s2"""
    
${"Generic aggregation functions"}
       

Apart from map-reduce and aggregation pipeline Mongo Streams supports basic mongodb aggregation functions. Supported
aggregation functions are: 

* count of document matching supplied query  ${ snippet { query("key2" >= 3) count }}                $count
* count of document with supplied query and read preference:  ${ snippet { query("key2" >= 3) from ReadPreference.Nearest count }}    
* count of all documents     ${ snippet { query() count }}                $countAll
* getting distinct values for supplied key ${ snippet { (query() distinct ("key")): ChannelResult[DBCollection, String] }}      $distinct 
        
      
    """


  def count = AggregationResult(query("key2" >= 3) count).verify {
    case a :: Nil => a must_== 2
    case other => ko("unexpected " + other)
  }

  def countAll = AggregationResult(query() count).verify {
    case a :: Nil => a must_== 4
    case other => ko("unexpected " + other)
  }

  def distinct = AggregationResult[String](query("key2" >= 2).distinct("key")).verify {
    result => result.toSet must haveTheSameElementsAs(Set("b", "c"))
  }


  case class AggregationResult[A: BSONSerializable](ch: ChannelResult[DBCollection, A]) {

    val mongo = new WithMongoCollection {}


    def verify(f: Seq[A] => MatchResult[Any]): MatchResult[Any] = {

      mongo.collection.insert(BSONObject("key" -> "a", "key2" -> 1))
      mongo.collection.insert(BSONObject("key" -> "b", "key2" -> 2))
      mongo.collection.insert(BSONObject("key" -> "b", "key2" -> 3))
      mongo.collection.insert(BSONObject("key" -> "c", "key2" -> 4))


      f((mongo.collection through ch).runLog.run.toList)
    }

  }


}
