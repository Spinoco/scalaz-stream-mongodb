package scalaz.stream.mongodb.update

import org.specs2.Specification
import org.specs2.specification.Snippets
import com.mongodb.{DBObject, BasicDBObjectBuilder, WriteConcern}
import org.bson.types.ObjectId
import org.specs2.matcher.MatchResult
import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.MongoRuntimeSpecification
import scalaz.stream.mongodb.channel.ChannelResult

import collection.JavaConverters._

class CollectionRemoveSpec extends Specification with Snippets with MongoRuntimeSpecification {
  def is =
    s2"""

      ${"Removing documents".title}

      Documents are removed from the collection by simple remove command:

      ${ snippet { query("key" === 1) and remove }}     $r1


      Additionally you can specify write concern for remove command by using `ensure`

      ${ snippet { query("key" === 1) and remove.ensure(WriteConcern.MAJORITY) }}


      Lastly, if the remove command on non sharded collection has to be isolated (this mean that no other updated may interleave during its execution),
      just add isolated modifier to it

       ${ snippet { query("key" === 1) and remove.isolated(true) }}


    """

  case class checkQuery(remove: ChannelResult[WriteResult]) {


    lazy val mongo = new WithMongoCollection()

    def document(v: Int) = BasicDBObjectBuilder.start("_id", new ObjectId).add("key", v).get

    def writeDocs(count: Int) = {
      val docs = for {i <- 0 until count} yield (document(i))
      docs.foreach { d => mongo.collection.save(d) }
      docs
    }

    def verifyRemove(f: (Seq[DBObject], Seq[DBObject]) => MatchResult[Any]): MatchResult[Any] = {
      if (mongo.collection.count() > 0) mongo.collection.drop
      val docs = writeDocs(5)

      (mongo.collection through remove).run.run

      val found = mongo.collection.find().iterator().asScala.toList

      f(docs, found)


    }

  }


  def r1 = checkQuery(query("key" >= 2) and remove).verifyRemove {
    case (in, result) =>
      result.size must_== 2
  }
}
