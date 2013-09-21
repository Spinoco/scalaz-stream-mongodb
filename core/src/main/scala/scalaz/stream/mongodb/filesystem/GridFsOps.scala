package scalaz.stream.mongodb.filesystem

import scalaz.syntax.Ops
import com.mongodb.DB
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task

import scalaz.stream.{Bytes, Process}
import scalaz.stream.processes._
import com.mongodb.gridfs.GridFS
import java.io.OutputStream
import scalaz.stream.Process._

import scalaz.stream.mongodb.collectionSyntax._


/**
 *
 * User: pach
 * Date: 9/19/13
 * Time: 9:09 PM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait GridFsOps extends Ops[GridFs] {


  /**
   * Provides Sink, that when run, will save supplied file with data. 
   */
  def write(file: MongoFile, chunkSize: Long = GridFS.DEFAULT_CHUNKSIZE): ChannelResult[DB, Bytes => Task[Unit]] = ChannelResult {
    import Task._
    Process.wrap {
      delay {
        db: DB => now {
          resource[OutputStream, Bytes => Task[Unit]](delay {
            val gfs = new GridFS(db, self.name)
            val gfsFile = gfs.createFile()
            gfsFile.setFilename(file.name)
            gfsFile.setId(file.id)
            file.contentType.foreach(gfsFile.setContentType(_))
            gfsFile.setMetaData(file.meta)
            gfsFile.setChunkSize(chunkSize)
            gfsFile.getOutputStream
          })({
            os => delay(os.close)
          })({
            os => now(bytes => now(os.write(bytes.bytes)))
          })
        }
      }
    }
  }


  /**
   * Will list all files that do satisfy given predicate  
   */
  def list(qry: FileQuery): ChannelResult[DB, MongoFileRead] = ChannelResult {
    import Task._

    val channel: Channel[Task, DB, Process[Task, MongoFileRead]] = {
      Process.wrap {
        now {
          db: DB => delay((db.getCollection(self.name + ".files") through qry.q) |> lift(MongoFileRead(_, self.name)))
        }
      }
    }

    channel
  }


}
                                                                 