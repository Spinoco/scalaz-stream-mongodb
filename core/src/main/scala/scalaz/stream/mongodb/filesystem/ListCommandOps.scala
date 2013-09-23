package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.stream.Process
import scalaz.stream.Process._
import scalaz.stream.processes._
import scalaz.concurrent.Task
import com.mongodb.gridfs.GridFS
import scalaz.stream.mongodb.collectionSyntax._

/**
 *
 * operations on list commands 
 */
trait ListCommandOps {

  val query: FileQuery

  private[filesystem] def makeListChannelResult: ChannelResult[GridFS, MongoFileRead] = ChannelResult {
    import Task._
    val channel: Channel[Task, GridFS, Process[Task, MongoFileRead]] = {
      Process.wrap {
        now {
          gfs: GridFS =>
            val bucket = gfs.getBucketName
            delay {

              (gfs.getDB.getCollection(bucket + ".files") through query.q) |> lift(MongoFileRead(_, bucket))
            }
        }
      }
    }

    channel
  }

}
