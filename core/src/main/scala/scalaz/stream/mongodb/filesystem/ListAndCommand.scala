package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.gridfs.GridFS
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.stream.processes._


/**
 * List command that runs supplied channel for all supplied files. 
 */
case class ListAndCommand[A](query: FileQuery
                             , ch: ChannelResult[GridFS, MongoFileRead => Process[Task, A]])
  extends ReadCommand[A] with ListCommandOps {

  def toChannelResult: ChannelResult[GridFS, A] =
    ListAndCommand.combine(makeListChannelResult |> take(1))(ch)

}

object ListAndCommand {

  def combine[A](ch1: ChannelResult[GridFS, MongoFileRead])(ch2: ChannelResult[GridFS, MongoFileRead => Process[Task, A]]): ChannelResult[GridFS, A] = {
    (ch1.zipWith(ch2) { (file, chf) => chf(file) }).flatMapProcess(v => v)
  }


}
