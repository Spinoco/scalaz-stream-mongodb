package scalaz.stream.mongodb.update

import org.specs2.Specification
import org.specs2.specification.Snippets
import com.mongodb.{DBCollection, WriteConcern, DBObject}
import org.specs2.matcher.MatchResult
import scalaz.stream.mongodb.MongoRuntimeSpecification
import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.channel.ChannelResult

class InsertAndSaveSpec extends Specification with Snippets with MongoRuntimeSpecification {

  val document: DBObject = BSONObject()

  def is =
    s2"""
      ${"Inserting documents to collection".title}
      
       
      Documents are inserted to collection via either insert or save commands. 
      Description of these commands of mongodb can be found in documentation 
      here [http://docs.mongodb.org/manual/reference/method/db.collection.save/] 
      and  here [http://docs.mongodb.org/manual/reference/method/db.collection.insert/]
      
      
      ${ snippet { insert(document) } }                                       $pureInsert
      ${ snippet { insert(document).ensure(WriteConcern.NORMAL) } }
  
      ${ snippet { save(document) } }
      ${ snippet { save(document).ensure(WriteConcern.NORMAL) } }             $pureSave
  
      There is a possibility to recover from insertion or update failures:
      
      ${ snippet { insert(document).toChannelResult attempt() }}               $pureInsertCatchException
       
       
    """

  def is2 = pureInsertCatchException


  def pureInsert = intoCollection(insert(BSONObject("key" -> 1))).andVerify {
    (present, wr) =>
      (present.size must_== 1) and
        (wr must beAnInstanceOf[InsertWriteResult])

  }

  def pureSave = intoCollection(save(BSONObject("key" -> 1))).andVerify {
    (present, wr) =>
      (present.size must_== 1) and
        (wr must beAnInstanceOf[InsertWriteResult])

  }

  def pureInsertCatchException = {
    val mongo = new WithMongoCollection()
    (mongo.collection through ensure(index("key" Ascending).unique(true))).run.run
    (mongo.collection through insert(BSONObject("key" -> 1))).run.run
    ((mongo.collection through insert(BSONObject("key" -> 1)).toChannelResult.attempt()).map(_.isLeft)).collect.run must_== Seq(true)
  }


  case class intoCollection(p: ChannelResult[DBCollection, WriteResult]) {

    lazy val mongo = new WithMongoCollection()

    def andVerify(f: ((Seq[DBObject], WriteResult) => MatchResult[Any])): MatchResult[Any] = {
      if (mongo.collection.count() > 0) mongo.collection.drop

      val result = (mongo.collection through p).runLast.run

      val present = (mongo.collection through query()).collect.run

      f(present, result.get)
    }

  }


}

