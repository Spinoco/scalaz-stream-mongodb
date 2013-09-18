package scalaz.stream.mongodb.index

import org.specs2.Specification

import scalaz.stream.mongodb.collectionSyntax._
import org.specs2.specification.Snippets
import scala.collection.JavaConverters._
import com.mongodb.BasicDBObjectBuilder
import scalaz.stream.mongodb.MongoRuntimeSpecification

/**
 *
 * User: pach
 * Date: 7/17/13
 * Time: 7:20 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
class CollectionEnsureIndexSpec extends Specification with Snippets with MongoRuntimeSpecification {

  def is = s2"""
  ${"Ensuring Index on collection".title}
  
  Indexes on collections may be created via ensure action:
  
  for example: 
  
  creates one index that will be constructed in background : ${ snippet { ensure(index("foo" -> Order.Ascending) background true) }}     
  creates one compound index that is sparse :                ${ snippet { ensure(index("foo" -> Order.Descending, "doo" -> Order.Ascending) sparse true) }}          
  
  All attributes that can be specified are found in [[http://docs.mongodb.org/manual/reference/method/db.collection.ensureIndex/#db.collection.ensureIndex]].            
  
  Operations: 
               
  $ensureNewIndex
  $ensurePresentIndex            
  
  
  """

  def ensureNewIndex = {
    "Index created when none is present" ! {
      val mongo = new WithMongoCollection()

      (mongo.collection through (ensure(index("foo" -> Order.Ascending) background false sparse true unique true name ("goosh")))).run.run

      val maybeIdx = mongo.collection.getIndexInfo.asScala.find(_.get("name") == "goosh")

      (maybeIdx mustNotEqual None) and
        (maybeIdx.get.get("unique") must_== true) and
        (maybeIdx.get.get("background") must_== false) and
        (maybeIdx.get.get("sparse") must_== true)

    }
  }

  def ensurePresentIndex = {
    "Index already created we silently skip it" ! {
      val mongo = new WithMongoCollection()

      mongo.collection.ensureIndex(BasicDBObjectBuilder.start().add("foo", -1).get, BasicDBObjectBuilder.start().add("name", "goosh").get)

      val start = mongo.collection.getIndexInfo.size()

      (mongo.collection through (ensure(index("foo" -> Order.Ascending) background false sparse true unique true name ("goosh")))).run.run

      val maybeIdx = mongo.collection.getIndexInfo.asScala.find(_.get("name") == "goosh")
      
      (start must_== 2) and
        (maybeIdx mustNotEqual None) and
        (maybeIdx.get.get("unique") must_== null) and
        (maybeIdx.get.get("background") must_== null) and
        (maybeIdx.get.get("sparse") must_== null)


    }
  }

}
