package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task
import scalaz.stream.Bytes
import scalaz.stream.Process
import scalaz.stream.processes._
import java.io.{FileNotFoundException, InputStream}
import com.mongodb.gridfs.GridFS


object FileUtil extends FileUtil

trait FileUtil {

  /** Produces ChannelResult, that will read single file **/
  def readFile(buffSize: Int = GridFS.DEFAULT_CHUNKSIZE): ChannelResult[GridFS, MongoFileRead => Process[Task, Bytes]] = ChannelResult {
    import Task._
    (gfs: GridFS) => now {
      (file: MongoFileRead) =>
        resource[(InputStream, Array[Byte]), Bytes](delay {
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
  val removeFile: ChannelResult[GridFS, MongoFileRead => Process[Task, Unit]] = ChannelResult {
    import Task._
    (gfs: GridFS) => now {
      (file: MongoFileRead) => Process.wrap(now {
        gfs.remove(file.id)
      })
    }
  }

}
