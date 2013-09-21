package scalaz.stream.mongodb.update

import org.specs2.Specification
import org.specs2.specification.Snippets
import org.bson.types.ObjectId
import scalaz.stream.Process
import com.mongodb.{BasicDBObjectBuilder, DBObject}
import org.specs2.matcher.MatchResult
import scalaz.stream.mongodb.MongoRuntimeSpecification
import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.channel.ChannelResult
import collection.JavaConverters._


class CollectionFindAndModifySpec extends Specification with Snippets with MongoRuntimeSpecification {

  def is =
    s2"""

       ${"Find And Modify".title}


       On mongoDB collection there is findAndModify command that allows user to return either original or updated document
       when performing document update.

       This is implemented in MongoStreams as two separate operations:

       ${ snippet { query("key1" === 1) and updateOne("key" := 33) }} $qu1

       and for document removal

       ${ snippet { query("key1" === 1) and removeOne }}              $qu2


       Both variants support additional modifiers:

       To return new document instead of old one:                     ${ snippet { query("key1" === 1) and updateOne("key" := 33).returnNew(true) }}              $qu3
       To first sort the documents before picking one to update:      ${ snippet { query().sort("key" Descending) and updateOne("key" := 33) }}                   $qu4
       To limit the keys in returned document using projection:       ${ snippet { query("key1" === 1).project("key") and updateOne("key" := 33) }}               $qu5

       To first sort documents before actually removing them:         ${ snippet { query().sort("key" Descending) and removeOne }}                                $qu6
       To limit the keys in returned document once removed:            ${ snippet { query("key1" === 1).project("key") and removeOne }}                            $qu7

    """


  def qu1 = updateWith(query("key" === 1) and updateOne("key" := 33)).andVerify {
    case (docs, result, r) =>
      result.find(_[Int]("key") == 33).isDefined must_== true and
        (r must_== docs.find(_[Int]("key") == 1))
  }

  def qu2 = updateWith(query("key" === 1) and removeOne).andVerify {
    case (docs, result, r) =>
      result.find(_[Int]("key") == 1).isDefined must_== false and
        (r must_== docs.find(_[Int]("key") == 1))
  }

  def qu3 = updateWith(query("key" === 1) and updateOne("key" := 33).returnNew(true)).andVerify {
    case (docs, result, r) =>
      result.find(_[Int]("key") == 33).isDefined must_== true and
        (r must_== result.find(_[Int]("key") == 33))
  }

  def qu4 = updateWith(query().sort("key" Descending) and updateOne("key" := 33)).andVerify {
    case (docs, result, r) =>
      (result.find(_[Int]("key") == 33).isDefined must_== true) and
        (r must_== docs.find(_[Int]("key") == 4))
  }

  def qu5 = updateWith(query("key" === 1).project("key") and updateOne("key" := 33)).andVerify {
    case (docs, result, r) =>
      (result.find(_[Int]("key") == 33).isDefined must_== true) and
        (r.map(_[ObjectId]("_id")) must_== docs.find(_[Int]("key") == 1).map(_[ObjectId]("_id"))) and
        (r.map(_.getAs[String]("key1")).flatten must_== None)
  }

  def qu6 = updateWith(query().sort("key" Descending) and removeOne).andVerify {
    case (docs, result, r) =>
      result.find(_[Int]("key") == 4).isDefined must_== false and
        (r must_== docs.find(_[Int]("key") == 4))
  }

  def qu7 = updateWith(query("key" === 1).project("key") and removeOne).andVerify {
    case (docs, result, r) =>
      result.find(_[Int]("key") == 1).isDefined must_== false and
        (r.map(_[ObjectId]("_id")) must_== docs.find(_[Int]("key") == 1).map(_[ObjectId]("_id"))) and
        (r.map(_.getAs[String]("key1")).flatten must_== None)
  }


  case class updateWith(up: ChannelResult[Option[DBObject]]) {

    lazy val mongo = new WithMongoCollection()


    def document(v: Int) = BasicDBObjectBuilder.start("_id", new ObjectId).add("key", v).add("key1", v * 2).get

    def writeDocs(count: Int) = {
      val docs = for {i <- 0 until count} yield (document(i))
      docs.foreach { d => mongo.collection.save(d) }
      docs
    }

    def andVerify(f: (Seq[DBObject], List[DBObject], Option[DBObject]) => MatchResult[Any]) = {

      if (mongo.collection.count() > 0) mongo.collection.drop
      val docs = writeDocs(5)

      val r = (mongo.collection through up).runLast.run

      val found = mongo.collection.find().iterator().asScala.toList

      f(docs, found, r.flatten)

    }

  }


}
