package scalaz.stream.mongodb.update

import com.mongodb.{DBCollection, WriteConcern, DBObject}
import scalaz.stream.mongodb.MongoCollectionCommand
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task

/**
 * Performs simple insert of document with optional write concern
 */
case class InsertAction(o: DBObject, wc: Option[WriteConcern] = None) extends MongoCollectionCommand[WriteResult] with ObjectIdSupport {
  def ensure(wc: WriteConcern) = copy(wc = Some(wc))

  def toChannelResult: ChannelResult[DBCollection,WriteResult] = ChannelResult {
    c => Task.delay {
      WriteResult(c.insert(assureId(o), wc.getOrElse(c.getWriteConcern)), o)
    }
  }

}
