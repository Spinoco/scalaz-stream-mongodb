package scalaz.stream.mongodb.index

import scalaz.stream.mongodb.MongoCommand
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task

 
case class EnsureIndex(idx: CollectionIndex) extends MongoCommand[Unit] {
  def toChannelResult: ChannelResult[Unit] =
    ChannelResult(c => Task.now(c.ensureIndex(idx.keysAsBson, idx.optionsAsBson)))
}
