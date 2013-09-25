package scalaz.stream.mongodb.query


import com.mongodb.{DBCollection, BasicDBObject, DBObject}


import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.MongoCollectionCommand
import scalaz.stream.Process
import scalaz.stream.Process._
import scalaz.concurrent.Task
import scalaz.stream.mongodb.channel.ChannelResult


/**
 * mongodb Query definition
 * @param bq               Basic query selector
 * @param where            Javascript `$where` selector
 * @param sort             Sort of the query
 * @param hint             Hints of the query
 * @param limit            Limit constrain on number of documents returned
 * @param skip             How much documents to ignore form the beginning of the query
 * @param projection       Which fields of the document to include
 * @param explainFlag      Explain result
 * @param snapshotFlag     Whether to create snapshot
 * @param comment          Eventual comment to use in the log
 * @param readPreference   ReadPreference of the query 
 */
case class Query(bq: BasicQuery,
                 where: Option[String] = None,
                 sort: Option[QuerySort] = None,
                 hint: Option[QueryHint] = None,
                 limit: Option[Int] = None,
                 skip: Option[Int] = None,
                 projection: Option[QueryProjection] = None,
                 explainFlag: Option[ExplainVerbosity.Value] = None,
                 snapshotFlag: Option[Boolean] = None,
                 comment: Option[String] = None,
                 readPreference: Option[ReadPreference] = None
                  ) extends MongoCollectionCommand[DBObject] with QueryOps {
  val self = this

  def toChannelResult: ChannelResult[DBCollection,DBObject] = {
    val channel: Channel[Task, DBCollection, Process[Task, DBObject]] =
      emit(Task.now {
        c: DBCollection =>
          Task.now {
            scalaz.stream.io.resource(
              Task.delay {
                val cursor = this.projection match {
                  case Some(p) => c.find(this.asDBObject, p.asDBObject)
                  case None => c.find(this.asDBObject)
                }
                this.skip.foreach(cursor.skip(_))
                this.limit.foreach(cursor.limit(_))
                this.readPreference.foreach(rp => cursor.setReadPreference(rp.asMongoDbReadPreference))
                cursor
              })(
              c => Task.delay(c.close()))(
              c => Task.delay {
                if (c.hasNext) {
                  c.next
                } else {
                  throw End
                }
              }
            )
          }
      }).eval

    ChannelResult(channel)
  }


  /**
   * Compiles query to mongodb DBOBject representation
   */
  lazy val asDBObject: DBObject = {
    val o = new BasicDBObject()
    o.put("$query", bq.o)
    where.foreach(o.put("$where",_))
    sort.foreach(s => o.put("$orderby", s.o))
    explainFlag.foreach {
      case ExplainVerbosity.Normal => o.put("$explain", true) //setting this to false disable explain. Not sure how to get normal non.verbose explain....
      case ExplainVerbosity.Verbose => o.put("$explain", true)
    }
    hint foreach {
      case QueryHintIndexName(name) => o.put("$hint", name)
      case QueryHintByKey(keys) =>
        val kso: DBObject = BSONObject()
        kso ++= keys.map((_, 1: BSONAny)).toMap
        o.put("$hint", kso)
    }
    snapshotFlag foreach (o.put("$snapshot", _))
    comment foreach (o.put("$comment", _))
    o
  }

}