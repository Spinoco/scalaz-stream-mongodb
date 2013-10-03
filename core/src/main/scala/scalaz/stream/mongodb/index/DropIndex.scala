package scalaz.stream.mongodb.index

import scalaz.stream.mongodb.MongoCollectionCommand
import scalaz.stream.mongodb.channel.ChannelResult

import scalaz.concurrent.Task
import com.mongodb.DBCollection


case class DropIndexByName(name: String) extends MongoCollectionCommand[Unit] {
  def toChannelResult: ChannelResult[DBCollection,Unit] =
    ChannelResult(c => Task.delay(c.dropIndex(name)))

}

case class DropIndexByKeys(idx: CollectionIndex) extends MongoCollectionCommand[Unit] {
  def toChannelResult: ChannelResult[DBCollection,Unit] =
    ChannelResult(c => Task.delay(c.dropIndex(idx.keysAsBson)))
}
