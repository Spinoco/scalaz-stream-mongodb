package scalaz.stream.mongodb.aggregate

import org.specs2.Specification
import org.specs2.specification.Snippets
import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.{MongoRuntimeSpecification, collectionSyntax}
import com.mongodb.{BasicDBObject, DBCollection, DBObject}
import scalaz.stream.mongodb.channel.ChannelResult
import org.specs2.matcher.MatchResult
import org.bson.types.ObjectId

import scala.language.reflectiveCalls

class PipelineSpec extends Specification with Snippets with MongoRuntimeSpecification {

  def is =
    s2"""
    
    ${"Aggregation Pipeline".title}
    
    
Mongodb allows to from so called aggregation pipeline as one of the basic methods in aggregation framework.  
Mongo Streams has simple syntactic sugar for these aggregation pipeline queries and commands. 
Supported commands are: 

* $$match   (`query` and `only` syntax)
* $$project
* $$limit
* $$skip
* $$unwind
* $$group
* $$sort

Below is description of syntax for every command supported


     
###  Limiting result with query and only    

Each aggregation command starts with initial query, that is turned to very first match. For example
${snippet { query("key" -> "value") |>> limit(1) }}  will turn to aggregation command in json  
`{ " $$match" : { "key" : "value" } }`   and  `{ " $$limit" : 1 }`.
 
Additionally multiple queries may be chained together with `only` syntax 
(please note the `collectionSyntax` prefix is not necessary):
 
${snippet {
      query("key" -> "value") |>> ( limit(5) |>> collectionSyntax.only("key2" === 3))
    }}  ${onlyTest.simple}    

`only` syntax is _converted_ to $$match in Aggregation pipeline.        


### Grouping documents 

Basic aggregation pipeline command is `group`. Group allows to pick unique `id` for the documents that may consist
of multiple keys found in document. Then it looks for documents that have this `id` same, and when found will for 
each document compute the aggregation expression. 

${snippet {
      // creates aggregation pipeline, that first filters all documents where `key == value` then it groups all 
      // returned documents based on content of `key1` and for each document where `key1` is same will count sum of values in 
      // `key3` that is stored in `sumKey` key.
      query("key" -> "value") |>> (group("grouped" -> "$key1") compute ("sumKey" sum "$key3"))

      //multiple computations may be specified on the query: 

      (query("key" -> "value") |>> (group("grouped" -> "$key1") compute("sumKey" sum "$key3", "avgKey" avg "$key4"))): ChannelResult[DBCollection, DBObject]

    }}

There are few grouping expressions for `group` command: 
       
* sum        : ${snippet { query() |>> (group("g" -> "$k") compute ("sumKey" sum "$key3")) }}       ${groupTest.sum}
* avg        : ${snippet { query() |>> (group("g" -> "$k") compute ("avgKey" avg "$key3")) }}       ${groupTest.avg}
* min        : ${snippet { query() |>> (group("g" -> "$k") compute ("minKey" min "$key3")) }}       ${groupTest.min}
* max        : ${snippet { query() |>> (group("g" -> "$k") compute ("maxKey" max "$key3")) }}       ${groupTest.max}
* last       : ${snippet { query() |>> (group("g" -> "$k") compute ("lastKey" last "$key3")) }}     ${groupTest.last}
* first      : ${snippet { query() |>> (group("g" -> "$k") compute ("firstKey" first "$key3")) }}   ${groupTest.first}
* addToSet   : ${snippet { query() |>> (group("g" -> "$k") compute ("setKey" addToSet "$key3")) }}  ${groupTest.addToSet}
* push       : ${snippet { query() |>> (group("g" -> "$k") compute ("listKey" push "$key3")) }}     ${groupTest.push}

       
### Sorting documents
       
Similarly as in plain queries, the aggregation pipeline allows the sorting of the documents. This is achieved
simply via sort syntax. Please note that sorting documents _before_ the aggregation pipeline has no effects.
${snippet {
      query("key" -> "value") |>> sort("key1" Ascending)
    }}  ${sortTest.simple}

### Limiting and skipping documents
       
To limit results or skip unwanted results, limit and skip aggregation pipelines can be used. Following example
will skip first 10 results and then will limit the query by 20 results. 
        
${snippet {
      query("key" -> "value") |>> (skip(10) |>> limit(20))
    }}  ${skipAndLimitTest.simple}      
       
       
### Unwinding the documents

If the documents contains field, that is of list type and eventually contains document, the `unwind` syntax allows to 
convert the field to documents, that will instead of that key of list type contain each entry from the list and all other
keys of the document.

${snippet {
      query("key" -> "value") |>> unwind("key2")
    }}  ${unwindTest.simple}
       

###  Projecting the result documents
 
Pipeline allows to transform resulting document with `project` syntax this allows you to include or exclude certain keys from document

 ${snippet {
      query("key" -> "value") |>> project("key2" include, "key3" exclude)
    }}         

Moreover there is a possibility to add whole new fields with their values or rename the fields:
      
* renaming the fields  :   ${snippet { query() |>> (project("key2" include, "nkey2" setTo "$key2")) }}              ${projectionTest.simple}    
 
      
      
       
       """

  def is2 = unwindTest.simple

  val documents = Seq[DBObject](
    BSONObject("_id" -> new ObjectId, "key" -> "A", "v1" -> 1)
    , BSONObject("_id" -> new ObjectId, "key" -> "A", "v1" -> 2)
    , BSONObject("_id" -> new ObjectId, "key" -> "B", "v1" -> 3)
    , BSONObject("_id" -> new ObjectId, "key" -> "B", "v1" -> 4)
    , BSONObject("_id" -> new ObjectId, "key" -> "C", "v1" -> 5)
    , BSONObject("_id" -> new ObjectId, "key" -> "C", "v1" -> 6)
  )


  def onlyTest = new {

    def simple = Pipeline(query("key" present) |>> (skip(2) |>> collectionSyntax.only("v1" %(2, 0)))).verify {
      (result, c) =>
        result must_== documents.drop(2).filter(_.as[Int]("v1") % 2 == 0)
    }

  }

  def groupTest = new {
    def sum = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" sum "$v1"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(Seq[DBObject](
          BSONObject("_id" -> BSONObject("key" -> "A"), "sumKey" -> 3)
          , BSONObject("_id" -> BSONObject("key" -> "B"), "sumKey" -> 7)
          , BSONObject("_id" -> BSONObject("key" -> "C"), "sumKey" -> 11)
        ))
    }

    def avg = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" avg "$v1"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(Seq[DBObject](
          BSONObject("_id" -> BSONObject("key" -> "A"), "sumKey" -> 1.5d)
          , BSONObject("_id" -> BSONObject("key" -> "B"), "sumKey" -> 3.5d)
          , BSONObject("_id" -> BSONObject("key" -> "C"), "sumKey" -> 5.5d)
        ))
    }

    def min = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" min "$v1"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(Seq[DBObject](
          BSONObject("_id" -> BSONObject("key" -> "A"), "sumKey" -> 1)
          , BSONObject("_id" -> BSONObject("key" -> "B"), "sumKey" -> 3)
          , BSONObject("_id" -> BSONObject("key" -> "C"), "sumKey" -> 5)
        ))
    }

    def max = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" max "$v1"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(Seq[DBObject](
          BSONObject("_id" -> BSONObject("key" -> "A"), "sumKey" -> 2)
          , BSONObject("_id" -> BSONObject("key" -> "B"), "sumKey" -> 4)
          , BSONObject("_id" -> BSONObject("key" -> "C"), "sumKey" -> 6)
        ))
    }


    def last = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" last "$v1"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(Seq[DBObject](
          BSONObject("_id" -> BSONObject("key" -> "A"), "sumKey" -> 2)
          , BSONObject("_id" -> BSONObject("key" -> "B"), "sumKey" -> 4)
          , BSONObject("_id" -> BSONObject("key" -> "C"), "sumKey" -> 6)
        ))
    }


    def first = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" first "$v1"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(Seq[DBObject](
          BSONObject("_id" -> BSONObject("key" -> "A"), "sumKey" -> 1)
          , BSONObject("_id" -> BSONObject("key" -> "B"), "sumKey" -> 3)
          , BSONObject("_id" -> BSONObject("key" -> "C"), "sumKey" -> 5)
        ))
    }


    def addToSet = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" addToSet "$v1"))).verify {
      (result, c) =>
        def extractOne(key: String): Set[Int] = {
          result.find(r => r.as[DBObject]("_id").as[String]("key") == key).map(_.as[Set[Int]]("sumKey")).getOrElse(Set())
        }

        (result must haveSize(3)) and
          (extractOne("A") must_== Set(1, 2)) and
          (extractOne("B") must_== Set(3, 4)) and
          (extractOne("C") must_== Set(5, 6))
    }

    def push = Pipeline(query() |>> (group("key" -> "$key") compute ("sumKey" push "$v1"))).verify {
      (result, c) =>
        def extractOne(key: String): List[Int] = {
          result.find(r => r.as[DBObject]("_id").as[String]("key") == key).map(_.as[List[Int]]("sumKey")).getOrElse(List())
        }

        (result must haveSize(3)) and
          (extractOne("A") must_== List(1, 2)) and
          (extractOne("B") must_== List(3, 4)) and
          (extractOne("C") must_== List(5, 6))
    }

  }

  def sortTest = new {

    def simple = Pipeline(query("key" present) |>> sort("key" Descending, "v1" Descending)).verify {
      (result, c) =>
        result must_== documents.sortBy(_.as[String]("key")).reverse
    }


  }

  def skipAndLimitTest = new {

    def simple = Pipeline(query("key" present) |>> (sort("key" Ascending) |>> skip(2) |>> limit(3))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(documents.sortBy(_.as[String]("key")).drop(2).take(3))
    }

  }

  def projectionTest = new {

    def simple = Pipeline(query() |>> project("key" include, "_id" exclude, "key2" setTo "$v1")).verify {
      (result, c) =>
        result must haveTheSameElementsAs(documents.map {
          o =>
            val no = new BasicDBObject()
            no.append("key", o.get("key"))
            no.append("key2", o.get("v1"))
            no
        })
    }


  }

  def unwindTest = new {

    def simple = Pipeline(query() |>> ((group("key" -> "$key") compute ("sumKey" addToSet "$v1")) |>> unwind("$sumKey"))).verify {
      (result, c) =>
        result must haveTheSameElementsAs(documents.map(o => {
          val no = new BasicDBObject
          no.append("_id", new BasicDBObject().append("key", o.get("key")))
          no.append("sumKey", o.get("v1"))
          no
        }))
    }

  }

  case class Pipeline(ch: ChannelResult[DBCollection, DBObject]) {

    val mongo = new WithMongoCollection {}

    def verify(f: (Seq[DBObject], DBCollection) => MatchResult[Any]) = {
      documents.foreach(mongo.collection.insert(_))
      f((mongo.collection through ch).runLog.run.toList, mongo.collection)
    }

  }


}
