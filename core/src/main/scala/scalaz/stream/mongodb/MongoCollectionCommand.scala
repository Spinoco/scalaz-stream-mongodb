package scalaz.stream.mongodb

import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.DBCollection


/**
 * Each operation on mongo shall result in this command
 * @tparam A Type of result. DBObject or command now
 */
trait MongoCollectionCommand[A] {

  def toChannelResult: ChannelResult[DBCollection,A]

}
