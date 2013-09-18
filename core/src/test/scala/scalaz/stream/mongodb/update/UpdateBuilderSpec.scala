package scalaz.stream.mongodb.update

import org.specs2.Specification
import org.specs2.specification.Snippets
import com.mongodb.{DBObject, BasicDBObjectBuilder}
import org.bson.types.ObjectId
import org.specs2.execute.SnippetParams
import scalaz.stream.mongodb.{MongoRuntimeSpecification, SnippetFormatter}
import scala.collection.JavaConverters._
import  scalaz.stream.mongodb.collectionSyntax._


class UpdateBuilderSpec extends Specification with Snippets with SnippetFormatter with MongoRuntimeSpecification {

  val document = BasicDBObjectBuilder.start("k", 1).add("_id", new ObjectId).get

  def testDoc =
    dbo
    .add("key", 10)
    .add("key2", "val1")
    .add("key3", 1)
    .add("key4", 3)
    .add("key5", Seq(1, 2, 3).asJava)
    .add("key6", Seq(
      dbo.add("n1", 1).add("n2", 4).get,
      dbo.add("n1", 3).add("n2", 2).get
    ).asJava).get

  lazy val examples = makeExamples

  case class updateMongo(update: UpdateAction, original: DBObject) {
    lazy val mongo = new WithMongoCollection()

    def verify(expectation: org.bson.BSONObject) = {
      if (mongo.collection.find().hasNext) mongo.collection.drop()

      mongo.collection.insert(original)

      ( mongo.collection through (query() and update)).run.run 

      val found = mongo.collection.findOne()
 

      ((found.get("key") must_== expectation.get("key")) and
        (found.get("key2") must_== expectation.get("key2")) and
        (found.get("key3") must_== expectation.get("key3")) and
        (found.get("key4") must_== expectation.get("key4")) and
        (found.getAs[List[Int]]("key5") must_== expectation.getAs[List[Int]]("key5")) and
        (found.getAs[List[Map[String, Int]]]("key6") must_== expectation.getAs[List[Map[String, Int]]]("key6")))
    }
  }
 

  implicit val sp = SnippetParams[(Int, UpdateAction)]().check {
    e => {
      val (expQuery, dbOrig, dbExpect) = examples(e._1)._2

      (e._2.raw.toString must_== expQuery.toString) and updateMongo(e._2, dbOrig).verify(dbExpect)

    }
  }

  def is = formatSnippets(
    s2"""
    ${"Update operation DSL"}
       
    Update operations allows atomic modification of documents within mongo collection. Following operations on documents are supported: 
       
    ${ snippet { 1 -> update(document) }.verify }   
    ${ snippet { 2 -> update("key" := 1) }.verify }  
    ${ snippet { 2 -> update("key" set 1) }.verify }
    ${ snippet { 3 -> update("key" := 1, "key2" := "string") }.verify }   
    ${ snippet { 4 -> update("key" += 1) }.verify }   
    ${ snippet { 4 -> update("key" inc 1) }.verify }  
    ${ snippet { 5 -> update("key" += 1, "key2" := "string", "key3" += 3, "key4" := 3) }.verify }   
    ${ snippet { 6 -> update("key" -, "key2" remove) }.verify }  
    ${ snippet { 7 -> update("key" ~> "keyR") }.verify }  
    ${ snippet { 7 -> update("key" renameTo "keyR") }.verify }  
    ${ snippet { 8 -> update("key" setOnInsert 1) }.verify }  
    ${ snippet { 9 -> update("key5" ++= Seq(1, 2, 3)) }.verify }  
    ${ snippet { 9 -> update("key5" push Seq(1, 2, 3)) }.verify }  
    ${ snippet { 10 -> update("key5" addToSet Set(2, 3, 4)) }.verify }  
    ${ snippet { 11 -> update(("key6" ++= Set[DBObject](BSONObject("n1" -> 1, "n2" -> 1))).sort("n2" Ascending).slice(5)) }.verify }  
    ${ snippet { 12 -> update(("key5" ++= Set(1, 2, 3)).slice(4)) }.verify }
    ${ snippet { 13 -> update("key5" --= Seq(1, 2)) }.verify } 
    ${ snippet { 14 -> update(pull("key5" >= 3)) }.verify }  
    ${ snippet { 15 -> update("key" & 7) }.verify }  
    ${ snippet { 16 -> update("key" | 7) }.verify }  
    ${ snippet { 17 -> update("key5" popLast) }.verify } 
    ${ snippet { 18 -> update("key5" popFirst) }.verify }  
    
   """, examples)


  def makeExamples: Map[Int, (String, (DBObject, DBObject, org.bson.BSONObject))] = Map(
    (1, ("Replacing the document with new one", (document, document, document))),
    (2, ("Updating single key with new value", (dbo.add("$set", dbo.add("key", 1).get).get, testDoc, testDoc +=("key", 1)))),
    (3, ("Updating single key with new value", (dbo.add("$set", dbo.add("key", 1).add("key2", "string").get).get, testDoc, testDoc +=("key", 1) +=("key2", "string")))),
    (4, ("Incrementing value of given key by one", (dbo.add("$inc", dbo.add("key", 1).get).get, testDoc, testDoc +=("key", 11)))),
    (5, ("Different update types may be mixed together", (dbo.add("$inc", dbo.add("key", 1).add("key3", 3).get).add("$set", dbo.add("key2", "string").add("key4", 3).get).get, testDoc, testDoc +=("key", 11) +=("key2", "string") +=("key3", 4) +=("key4", 3)))),
    (6, ("Removing given key", (dbo.add("$unset", dbo.add("key", "").add("key2", "").get).get, testDoc, testDoc --= Seq("key", "key2")))),
    (7, ("Renaming given key", (dbo.add("$rename", dbo.add("key", "keyR").get).get, testDoc, testDoc -= "key" +=("keyR", 10)))),
    (8, ("Update key only when upserting", (dbo.add("$setOnInsert", dbo.add("key", 1).get).get, testDoc, testDoc))),
    (9, ("Adds values to given key", (dbo.add("$push", dbo.add("key5", dbo.add("$each", Seq(1, 2, 3).asJava).get).get).get, testDoc, testDoc +=("key5", Seq(1, 2, 3, 1, 2, 3))))),
    (10, ("Adds values to given key as set", (dbo.add("$addToSet", dbo.add("key5", dbo.add("$each", Set(2, 3, 4).asJava).get).get).get, testDoc, testDoc +=("key5", Set(1, 2, 3, 4))))),
    (11, ("Adds values and sorts them", (dbo.add("$push", dbo.add("key6", dbo.add("$each", Set(dbo.add("n1", 1).add("n2", 1).get).asJava).add("$sort", dbo.add("n2", 1).get).add("$slice", -5).get).get).get, testDoc, testDoc +=("key6", Seq(dbo.add("n1", 1).add("n2", 1).get, dbo.add("n1", 3).add("n2", 2).get, dbo.add("n1", 1).add("n2", 4).get))))),
    (12, ("Adds values and limits array size", (dbo.add("$push", dbo.add("key5", dbo.add("$each", Set(1, 2, 3).asJava).add("$slice", -4).get).get).get, testDoc, testDoc +=("key5", Seq(3, 1, 2, 3))))),
    (13, ("Removes all specified values from given key", (dbo.add("$pullAll", dbo.add("key5", Seq(1, 2).asJava).get).get, testDoc, testDoc +=("key5", Seq(3))))),
    (14, ("Removes all values matching query from given key", (dbo.add("$pull", dbo.add("key5", dbo.add("$gte", 3).get).get).get, testDoc, testDoc +=("key5", Seq(1, 2))))),
    (15, ("Performs bitwise and", (dbo.add("$bit", dbo.add("key", dbo.add("and", 7).get).get).get, testDoc, testDoc +=("key", 10 & 7)))),
    (16, ("Performs bitwise or", (dbo.add("$bit", dbo.add("key", dbo.add("or", 7).get).get).get, testDoc, testDoc +=("key", 10 | 7)))),
    (17, ("Pops last item in array", (dbo.add("$pop", dbo.add("key5", 1).get).get, testDoc, testDoc +=("key5", Seq(1, 2))))),
    (18, ("Pops first item in array", (dbo.add("$pop", dbo.add("key5", -1).get).get, testDoc, testDoc +=("key5", Seq(2, 3))))),

    (1000000, ("Replacing the document with new one", (document, document, document)))
  )

  def dbo = new BasicDBObjectBuilder


}
