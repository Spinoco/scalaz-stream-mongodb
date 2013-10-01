package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.stream.processes._
import java.io.{FileNotFoundException, InputStream}
import com.mongodb.gridfs.GridFS
import scalaz.stream.Process.End
import scalaz.stream.mongodb.util.Bytes


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
            if (read >= 0) {
              new Bytes(buff, read)
            } else {
              throw End
            }
          }
        })
    }
  }

  /** Produces ChannelResult, that will remove single file. `true` is returned when file was removed **/
  val removeFile: ChannelResult[GridFS, MongoFileRead => Process[Task, Unit]] = ChannelResult {
    import Task._
    Process.eval {
      now {
        (gfs: GridFS) => now {
          Process.repeatEval {
            now {
              (file: MongoFileRead) => Process.eval(now {
                gfs.remove(file.id)
              })
            }
          }
        }
      }
    }

  }

}
