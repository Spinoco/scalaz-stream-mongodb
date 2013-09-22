package scalaz.stream.mongodb.index

import com.mongodb.{BasicDBObjectBuilder, DBObject}
import org.specs2.Specification
import org.specs2.specification.Snippets
import org.specs2.execute.SnippetParams
import scalaz.stream.mongodb.SnippetFormatter

import collection.JavaConverters._
import scalaz.stream.mongodb.collectionSyntax._
import Order._


class IndexBuilderSpec extends Specification with Snippets with SnippetFormatter {

  lazy val examples = makeExamples


  implicit val sp = SnippetParams[(Int, CollectionIndex)]().check {
    e => {
      (e._2.keysAsBson.toString must_== examples(e._1)._2._1.toString) and
        (e._2.optionsAsBson must_== examples(e._1)._2._2)
    }
  }


  def is = {
    formatSnippets(
      s2"""

     ${"Collection Indexes".title}


     Collection indexes can be specified with index builder that provides simple expressive syntax.

     Index examples (assuming `import collection.Order._`):

     ${ snippet { 1 -> index("foo" -> Ascending) }.verify }
     ${ snippet { 2 -> index("foo" -> Ascending, "boo" -> Descending) }.verify }
     ${ snippet { 3 -> index("foo" -> Ascending).background(true) }.verify }
     ${ snippet { 4 -> index("foo" -> Ascending).unique(true) }.verify }
     ${ snippet { 5 -> index("foo" -> Ascending).name("indexName") }.verify }
     ${ snippet { 6 -> index("foo" -> Ascending).dropDups(true) }.verify }
     ${ snippet { 7 -> index("foo" -> Ascending).sparse(true) }.verify }
     ${ snippet { 8 -> index("foo" -> Ascending).expireAfterSeconds(10) }.verify }
     ${ snippet { 9 -> index("foo" -> Ascending).version(1) }.verify }
     ${ snippet { 10 -> index("foo" -> Ascending).weights("items" -> 2) }.verify }
     ${ snippet { 11 -> index("foo" -> Ascending).defaultLanguage("english") }.verify }
     ${ snippet { 12 -> index("foo" -> Ascending).languageOverride("english") }.verify }
     ${ snippet { 20 -> index("foo" -> Ascending).sparse(true).unique(true).name("indexName") }.verify }
    """, examples)


  }

  def makeExamples: Map[Int, (String, (DBObject, DBObject))] = Map(
    (1, ("Simple one key index", ($keys("foo" -> 1), $index()))),
    (2, ("Simple two key index", ($keys("foo" -> 1, "boo" -> -1), $index()))),
    (3, ("Index to be build in background", ($keys("foo" -> 1), $index("background" -> true)))),
    (4, ("Index that is unique", ($keys("foo" -> 1), $index("unique" -> true)))),
    (5, ("Index that has name", ($keys("foo" -> 1), $index("name" -> "indexName")))),
    (6, ("Index that drop dups", ($keys("foo" -> 1), $index("dropDups" -> true)))),
    (7, ("Index that is sparse", ($keys("foo" -> 1), $index("sparse" -> true)))),
    (8, ("Index that expires document after some time", ($keys("foo" -> 1), $index("expireAfterSeconds" -> 10)))),
    (9, ("Index that is version 1", ($keys("foo" -> 1), $index("v" -> 1)))),
    (10, ("Index that has weight on items == 2", ($keys("foo" -> 1), $index("weights" -> dbo.add("items", 2).get)))),
    (11, ("Index that has default language english", ($keys("foo" -> 1), $index("defaultLanguage" -> "english")))),
    (12, ("Index that has override default language", ($keys("foo" -> 1), $index("languageOverride" -> "english")))),

    (20, ("Chained builder (sparse,unique,named)", ($keys("foo" -> 1), $index("sparse" -> true, "unique" -> true, "name" -> "indexName"))))
  )

  def $index(kv: (String, Any)*): DBObject =
    BasicDBObjectBuilder.start(
      kv.toMap.asJava
    ).get

  def $keys(kv: (String, Int)*): DBObject =
    BasicDBObjectBuilder.start(
      kv.toMap.asJava
    ).get


  def dbo = BasicDBObjectBuilder.start()


}
