package scalaz.stream.mongodb.filesystem

import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.gridfs.GridFS


/**
 * Command that list files
 * @param query
 */
case class ListCommand(query: FileQuery) extends ReadCommand[MongoFileRead] with ListCommandOps {

  def toChannelResult: ChannelResult[GridFS, MongoFileRead] = makeListChannelResult

  def and[A](ch: ChannelResult[GridFS, MongoFileRead => Process[Task, A]]): ListAndCommand[A] = ListAndCommand(query, ch)

  def foreach[A](ch: ChannelResult[GridFS, MongoFileRead => Process[Task, A]]): ListForEachCommand[A] = ListForEachCommand(query, ch)

  
}





