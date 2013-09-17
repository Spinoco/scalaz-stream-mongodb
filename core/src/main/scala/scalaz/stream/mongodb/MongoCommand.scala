package scalaz.stream.mongodb

import scalaz.stream.mongodb.channel.ChannelResult


/**
 * Each operation on mongo shall result in this command
 * @tparam A Type of result. DBObject or command now
 */
trait MongoCommand[A] {

  def toChannelResult: ChannelResult[A]

}
