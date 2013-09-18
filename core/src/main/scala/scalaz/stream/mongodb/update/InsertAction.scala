package scalaz.stream.mongodb.update

import com.mongodb.{WriteConcern, DBObject}
import scalaz.stream.mongodb.MongoCommand
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task

/**
 * Performs simple insert of document with optional write concern
 */
case class InsertAction(o: DBObject, wc: Option[WriteConcern] = None) extends MongoCommand[WriteResult] with ObjectIdSupport {
  def ensure(wc: WriteConcern) = copy(wc = Some(wc))

  def toChannelResult: ChannelResult[WriteResult] = ChannelResult {
    c => Task.now {
      WriteResult(c.insert(assureId(o), wc.getOrElse(c.getWriteConcern)), o)
    }
  }

}
