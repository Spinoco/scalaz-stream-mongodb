package scalaz.stream.mongodb.update

import com.mongodb.{WriteConcern, DBObject}
import scalaz.stream.mongodb.MongoCommand
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task

/**
 * Saves the supplied document with optional write concern 
 */
case class SaveAction(o: DBObject, wc: Option[WriteConcern] = None) extends MongoCommand[WriteResult] with ObjectIdSupport {
  def ensure(wc: WriteConcern) = copy(wc = Some(wc))

  def toChannelResult: ChannelResult[WriteResult] = ChannelResult {
    c => Task.now {
      WriteResult(c.save(assureId(o), wc.getOrElse(c.getWriteConcern)), o)
    }
  }

}
