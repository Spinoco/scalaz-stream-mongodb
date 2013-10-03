package scalaz.stream.mongodb.update

import com.mongodb.{DBCollection, WriteConcern, DBObject}
import scalaz.stream.mongodb.MongoCollectionCommand
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task

/**
 * Saves the supplied document with optional write concern 
 */
case class SaveAction(o: DBObject, wc: Option[WriteConcern] = None) extends MongoCollectionCommand[WriteResult] with ObjectIdSupport {
  def ensure(wc: WriteConcern) = copy(wc = Some(wc))

  def toChannelResult: ChannelResult[DBCollection,WriteResult] = ChannelResult {
    c => Task.delay {
      WriteResult(c.save(assureId(o), wc.getOrElse(c.getWriteConcern)), o)
    }
  }

}
