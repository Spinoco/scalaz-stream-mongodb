package scalaz.stream.mongodb.index

import scalaz.stream.mongodb.MongoCollectionCommand
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task
import com.mongodb.DBCollection


case class EnsureIndex(idx: CollectionIndex) extends MongoCollectionCommand[Unit] {
  def toChannelResult: ChannelResult[DBCollection,Unit] =
    ChannelResult(c => Task.now(c.ensureIndex(idx.keysAsBson, idx.optionsAsBson)))
}
