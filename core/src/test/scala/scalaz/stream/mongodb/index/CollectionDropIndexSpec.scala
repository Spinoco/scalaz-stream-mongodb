package scalaz.stream.mongodb.index

import org.specs2.Specification
import org.specs2.specification.Snippets

import scalaz.stream.mongodb.collectionSyntax._
import com.mongodb.BasicDBObjectBuilder
import scala.collection.JavaConverters._
import scalaz.stream.mongodb.MongoRuntimeSpecification

/**
 *
 * User: pach
 * Date: 7/14/13
 * Time: 8:46 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
class CollectionDropIndexSpec extends Specification with Snippets with MongoRuntimeSpecification {
  def is = s2"""
    ${"Dropping Index on collection".title}
       
    Any index on collection may be dropped via drop action.
    
    For example: 
    
    dropping index with specified name :                   ${ snippet { drop("indexName") } }
    dropping index on given collection keys :              ${ snippet { drop(index("foo" -> Order.Ascending, "boo" -> Order.Descending)) } }

    Operations:         

    $dropIndexWithName
    $dropIndexWithKeys

    """


  def dropIndexWithName = {
    "Drop index with name" ! {
      val mongo = new WithMongoCollection()

      mongo.collection.ensureIndex(new BasicDBObjectBuilder().add("foo", 1).get, "fooIndex")

      (mongo.collection through drop("fooIndex")).run.run

      mongo.collection.getIndexInfo.asScala.size must_== 1

    }
  }

  def dropIndexWithKeys = {
    "Drop index with keys" ! {

      val mongo = new WithMongoCollection()

      mongo.collection.ensureIndex(new BasicDBObjectBuilder().add("foo", 1).get)

      (mongo.collection through drop(index("foo" -> Order.Ascending))).run.run

      mongo.collection.getIndexInfo.asScala.size must_== 1

    }
  }


}
