package scalaz.stream.mongodb.index

import scalaz.stream.mongodb.MongoCommand
import scalaz.stream.mongodb.channel.ChannelResult

import scalaz.concurrent.Task


case class DropIndexByName(name: String) extends MongoCommand[Unit] {
  def toChannelResult: ChannelResult[Unit] =
    ChannelResult(c => Task.now(c.dropIndex(name)))

}

case class DropIndexByKeys(idx: CollectionIndex) extends MongoCommand[Unit] {
  def toChannelResult: ChannelResult[Unit] =
    ChannelResult(c => Task.now(c.dropIndex(idx.keysAsBson)))
}
