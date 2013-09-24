package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.gridfs.GridFS
import scalaz.stream.Process
import scalaz.stream.Process._
import scalaz.concurrent.Task

/**
 * ListCommand that runs supplied channel for every file listed 
 */
case class ListForEachCommand[A](query: FileQuery
                                 , ch: ChannelResult[GridFS, MongoFileRead => Process[Task, A]])
  extends ReadCommand[(MongoFileRead, Process[Task, A])] with ListCommandOps {

  def toChannelResult: ChannelResult[GridFS, (MongoFileRead, Process[Task, A])] =
    ListForEachCommand.combine(makeListChannelResult)(ch)
}

object ListForEachCommand {

  def combine[A](ch1: ChannelResult[GridFS, MongoFileRead])(ch2: ChannelResult[GridFS, MongoFileRead => Process[Task, A]]): ChannelResult[GridFS, (MongoFileRead, Process[Task, A])] = {
    val ch3 = ChannelResult(ch2.channel.flatMap(fa => repeatWrap(Task.now((gfs: GridFS) => fa(gfs)))))
    (ch1).zipWith(ch3) { (file, chf) => (file, chf(file)) }
  }


}
