package scalaz.stream.mongodb.query

import com.mongodb._
import org.bson.types.ObjectId
import org.scalacheck.{Prop, Arbitrary, Gen}
import org.specs2.{ScalaCheck, Specification}

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.MongoRuntimeSpecification
import scalaz.stream.mongodb.bson.BSONSerializable
import org.specs2.matcher.MatchResult

import scala.collection.JavaConverters._
import scalaz.syntax.Ops
import org.specs2.specification.Snippets

import scala.language.reflectiveCalls
import scala.language.implicitConversions
import scala.Some


class CollectionQuerySpec extends Specification with Snippets with MongoRuntimeSpecification with ScalaCheck {

  def is = {

    s2"""
    ${"Querying on mongoDB collection".title}

    Basic construct to query on mongo is `query` command. Query command creates process of BSONObjects, that can be further
    processed by scalaz.stream process combinators. Query supports simple DSL that can be further combined to produce remove, update and modify operations.

    to make a simple query that returns process of BSONObjects you can do

    ${ snippet { query("key" -> "value") } }  $q1

    if you want limit the amount of the documents returned, you can use `limit` and `skip` combinators

    ${ snippet { query("key" -> "value") skip 1 limit 3 }}   $q2

    if you want to sort documents before returning them back, you can use `sort` combinator. Sort combinator allows you to specify order and multiple keys
    that will be used when sorting. Please note two syntax for ordering allowed

    ${ snippet { query("key" -> "value") sort("key" Ascending, "otherKey" -> Order.Descending) }}      $q3
    ${ snippet { query("key" -> "value") orderby("key" Ascending, "otherKey" -> Order.Descending) }}   $q4

    # Replica sets #

    MongoDb allows various functionality when working with replica sets. Essentially, the possible options are described in ReadPreference in [[http://docs.mongodb.org/manual/core/read-preference/]].
    All of these options are supported via `from` combinator:

    ${ snippet {
      import scalaz.stream.mongodb.query.ReadPreference._
      query("key" -> "value") from Nearest.preferred(false)
    }}


    In mongoDB environments you may potentially want to read certain data only with specific members. This can be be achieved by tagging the read preference :

    ${ snippet {
      import scalaz.stream.mongodb.query.ReadPreference._
      query("key" -> "value") from Nearest.preferred(true).tags("disk" -> "ssd", "memory" -> "huge")
    }}

    # Projections #

    For the complex documents you maybe want to get only certain data as a result of the query to reduce time when deserializing data. This is achieved
    with project combinator:

    ${ snippet { query("key" -> "value") project("key1", "key2" $, "key3" first 4, "key4" last 6, "key5" skip 6 limit 5, "key6" elementMatch ("ek" -> 1)) }}

    As you may see projection combinator acepts many form of limiting the content of documents returned. Lets describe their meaning
    For more information you can see detailed description of their functionality in mongoDB docs [[http://docs.mongodb.org/manual/reference/operator/projection/]]

    To return only key1 from documents  :                     ${ snippet { query() project ("key1") }}                               ${projections.q1}
    To return only first element of any array under key1:     ${ snippet { query("key3" >= 1) project ("key3" $) }}                  ${projections.q2}
    To return only first 4 elements of any array under key1:  ${ snippet { query() project ("key1" first 4) }}                       ${projections.q3}
    To return only last 4 elements of any array under key1:   ${ snippet { query() project ("key1" last 4) }}                        ${projections.q4}
    To return elements 4-6 from an array under key1:          ${ snippet { query() project ("key1" skip 3 limit 3) }}                ${projections.q5}
    To return elements that have ek == 1 under key 1:         ${ snippet { query() project ("key1" elementMatch ("ek" === 1)) }}     ${projections.q6}


    # Explain Query #

    To see the actual indexes that are consulted when running the query and additional statistics, query may be run in explain mode.
    See [[http://docs.mongodb.org/manual/reference/method/cursor.explain/#cursor.explain]]:

    ${ snippet { query("key" -> "value") explain (ExplainVerbosity.Normal) }}   ${explain.normal}

    or

    ${ snippet { query("key" -> "value") explain (ExplainVerbosity.Verbose) }}  ${explain.verbose}


    # Query Hints #

    Query can contain index hints to choose appropriate index for given query.

    This indicates index named "indexName" shall be used

    ${ snippet { query("key" -> "value") hint ("indexName") }}                    ${explain.withHintByName}

    or this picks index on "key1" to be used with query

    ${ snippet { query("key" -> "value") hint (index("key1" Ascending)) }}        ${explain.withHintByKeys}


    # Query snapshot #

    Query can be in "snapshot" mode to make sure that documents are not returned multiple times when iterating over results.
    Queries with size less than 1M are automatically in snapshot mode.

    ${ snippet { query("key" -> "value") snapshot (true) }}


    # Query comment #

    Query can be commented to have given comment appear in profile log

    ${ snippet { query("key1" -> "value") comment ("Long duration query") }}


    """


  }

  def is2 = s2"""${explain.withHintByKeys}"""

  implicit class DocsOps(val self: Seq[DBObject]) extends Ops[Seq[DBObject]] {
    def hk[A: BSONSerializable](s: String): A = self.head.getAs[A](s).get

    def q(on: String): Query = scalaz.stream.mongodb.collectionSyntax.query(on -> self.hk[String](on))

    def query(on: String): Query = scalaz.stream.mongodb.collectionSyntax.query(on -> self.hk[String](on))

    def s(on: String): Seq[DBObject] = self.filter(d => d.getAs[String](on) == Some(self.hk[String](on))).toSeq

  }

  def q1 =
    QueryOf(_.query("key1"))
    .mustBeSameAs((d, c) => c.find(DBO.append("key1", d.hk[String]("key1"))))

  def q2 =
    QueryOf(_ => query() skip 1 limit 3)
    .mustBeSameAs((_, c) => c.find().skip(1).limit(3))

  def q3 =
    QueryOf(_ => query() sort ("key1" Ascending))
    .mustBeSameAs((_, c) => c.find().sort(DBO.append("key1", 1)))

  def q4 =
    QueryOf(_ => query() sort ("key1" Descending))
    .mustBeSameAs((_, c) => c.find().sort(DBO.append("key1", -1)))


  def projections = new {

    def q1 =
      QueryOf(_.query("key1") project ("key1"))
      .mustBeSameAs((d, c) => c.find(DBO.append("key1", d.hk[String]("key1")), DBO.append("key1", 1)))


    def q2 =
      QueryOf(d => query("key1" -> d.hk[String]("key1"), "key3" >= 1) project ("key3" $))
      .mustBeSameAs((d, c) => c.find(DBO.append("key1", d.hk[String]("key1")).append("key3", DBO.append("$gte", 1)), DBO.append("key3.$", 1)))

    def q3 =
      QueryOf(_ => query() project ("key3" first 4))
      .mustBeSameAs((_, c) => c.find(DBO, DBO.append("key3", DBO.append("$slice", 4))))

    def q4 =
      QueryOf(_ => query() project ("key3" last 4))
      .mustBeSameAs((_, c) => c.find(DBO, DBO.append("key3", DBO.append("$slice", -4))))

    def q5 =
      QueryOf(_ => query() project ("key3" skip 3 limit 3))
      .mustBeSameAs((_, c) => c.find(DBO, DBO.append("key3", DBO.append("$slice", List(3, 3).asJava))))


    def q6 =
      QueryOf(_ => query() project ("key4" elementMatch ("a1" >= 1)))
      .mustBeSameAs((_, c) => c.find(DBO, DBO.append("key4", DBO.append("$elemMatch", DBO.append("a1", DBO.append("$gte", 1))))))

  }

  def explain = new {

    def normal =
      ExplainQueryOf(_ => query() explain (ExplainVerbosity.Normal))
      .mustBeLikeA((_, c) => c.find().explain(), _.as[String]("cursor") must_== _.as[String]("cursor"))

    def verbose =
      ExplainQueryOf(_ => query() explain (ExplainVerbosity.Verbose))
      .mustBeLikeA((_, c) => c.find().explain(), _.as[String]("cursor") must_== _.as[String]("cursor"))

    def withHintByName =
      ExplainQueryOf(_ => query() hint ("idx_key1") explain (ExplainVerbosity.Normal))
      .prepare(_.ensureIndex(BSONObject("key1" -> 1), "idx_key1"))
      .mustBeLikeA((_, c) => c.find().hint("idx_key1").explain(), _.as[String]("cursor") must_== _.as[String]("cursor"))

    def withHintByKeys =
      ExplainQueryOf(_ => query() hint (index("key1" Descending) name ("idx_key1")) explain (ExplainVerbosity.Normal))
      .prepare(_.ensureIndex(BSONObject("key1" -> 1), "idx_key1"))
      .mustBeLikeA((_, c) => c.find().hint(DBO.append("key1", 1)).explain(), _.as[String]("cursor") must_== _.as[String]("cursor"))


  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SUPPORT
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  implicit lazy val oidGen = Gen[ObjectId] { p => Some(new ObjectId(p.rng.nextInt(), p.rng.nextInt(), p.rng.nextInt())) }

  val listOfObjectGen = for {
    a1 <- Arbitrary.arbInt.arbitrary
    a2 <- Arbitrary.arbInt.arbitrary
  } yield (BasicDBObjectBuilder.start(Map(
      ("a1" -> a1),
      ("a2" -> a2)
    ).asJava).get)

  val singleObjectGen = for {
    id <- oidGen
    key1 <- Arbitrary.arbitrary[String] suchThat (_.nonEmpty) //Gen.alphaStr suchThat (_.nonEmpty)
    key2 <- Arbitrary.arbitrary[String] suchThat (_.nonEmpty)
    key3size <- Gen.choose[Int](1, 20)
    key3 <- Gen.containerOfN[List, Int](key3size, Arbitrary.arbInt.arbitrary)
    key4size <- Gen.choose[Int](1, 20)
    key4 <- Gen.containerOfN[List, DBObject](key3size, listOfObjectGen)
  } yield (BasicDBObjectBuilder.start(Map(
      ("_id" -> id),
      ("key1" -> key1),
      ("key2" -> key2),
      ("key3" -> key3.asJava),
      ("key4" -> key4.asJava)
    ).asJava).get)


  def saveDocuments(to: DBCollection, docs: Seq[DBObject]) = {
    docs.foreach(to.save)
    // println("-" * 100)
    // println("#", docs.size, to.find.count())
    // to.find().iterator().asScala.foreach(println)
    //  println("-" * 100)
    ()
  }

  implicit val objectsGen: Arbitrary[Seq[DBObject]] = Arbitrary {
    for {
      size <- Gen.choose[Int](1, 20)
      seq <- Gen.containerOfN[List, DBObject](size, singleObjectGen)
    } yield (seq)

  }

  case class QueryOf(qf: Seq[DBObject] => Query) {
    lazy val mongo = new WithMongoCollection() {}

    def mustBeSameAs(vf: (Seq[DBObject], DBCollection) => DBCursor) = {
      Prop.forAll {
        (documents: Seq[DBObject]) => (documents.size > 0) ==> {
          //println("\n"*5)

          val q = qf(documents)

          saveDocuments(mongo.collection, documents)

          val r = (mongo.collection through q).collect.run

          val expected = vf(documents, mongo.collection).iterator().asScala.toList
          // r.foreach(println)
          // println("====="*100)
          // expected.foreach(println)

          expected must_== r

        }
      }
    }

  }

  case class ExplainQueryOf(qf: Seq[DBObject] => Query, before: Option[DBCollection => Unit] = None) {
    lazy val mongo = new WithMongoCollection() {}

    def prepare(f: (DBCollection) => Unit): ExplainQueryOf = {
      copy(before = Some(f))
    }

    def mustBeSameAs(vf: (Seq[DBObject], DBCollection) => DBObject) = mustBeLikeA(vf, _ must_== _)

    def mustBeLikeA(vf: (Seq[DBObject], DBCollection) => DBObject, verify: (DBObject, DBObject) => MatchResult[Any]) = {

      Prop.forAll {
        (documents: Seq[DBObject]) => (documents.size > 0) ==> {
          //println("\n"*5)

          before.foreach(_(mongo.collection))

          val q = qf(documents)

          saveDocuments(mongo.collection, documents)

          val r = (mongo.collection through q).runLastOr(DBO).run

          val expected = vf(documents, mongo.collection)
          // r.foreach(println)
          // println("====="*100)
          // expected.foreach(println)

          verify(r, expected)

        }
      }

    }

  }


  def DBO = new BasicDBObject()

}
