package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task
import com.mongodb.DB
import scalaz.stream.Bytes
import scalaz.stream.Process
import scalaz.stream.processes._
import java.io.{FileNotFoundException, InputStream}
import com.mongodb.gridfs.GridFS

/**
 *
 * User: pach
 * Date: 9/21/13
 * Time: 9:34 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
object FileUtil {

  /** Produces ChannelResult, that will read single file **/
  def singleFileReader(buffSize: Int): ChannelResult[DB, MongoFileRead => Process[Task, Bytes]] = ChannelResult {
    import Task._
    (db: DB) => now {
      (file: MongoFileRead) =>
        resource[(InputStream, Array[Byte]), Bytes](delay {
          val gfs = new GridFS(db, file.fileSystem)
          Option(gfs.findOne(file.id)) match {
            case Some(found) => (found.getInputStream, new Array(buffSize))
            case None => throw new FileNotFoundException()
          }
        })({
          case (is, _) => delay { is.close }
        })({
          case (is, buff) => delay {
            val read = is.read(buff)
            new Bytes(buff, read)
          }
        })
    }
  }

  /** Produces ChannelResult, that will remove single file. `true` is returned when file was removed **/
  val singleFileRemove: ChannelResult[DB, MongoFileRead => Process[Task, Unit]] = ChannelResult {
    import Task._
    (db: DB) => now {
      (file: MongoFileRead) => Process.wrap(now {
        val gfs = new GridFS(db, file.fileSystem)
        gfs.remove(file.id) 
      })
    }
  }

}
