package scalaz.stream.mongodb.filesystem

import scalaz.concurrent.Task
import scalaz.stream.{Process, Bytes}
import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.gridfs.GridFS
import scalaz.stream.processes._
import java.io.OutputStream

/**
 * write command 
 */
case class WriteCommand(file: MongoFileWrite) extends GridFsCommand[Bytes => Task[Unit]] {
  def toChannelResult: ChannelResult[GridFS, Bytes => Task[Unit]] = ChannelResult {
    import Task._
    Process.wrap {
      delay {
        gfs: GridFS => now {
          resource[OutputStream, Bytes => Task[Unit]](delay {
            val gfsFile = gfs.createFile()
            gfsFile.setFilename(file.name)
            gfsFile.setId(file.id)
            file.contentType.foreach(gfsFile.setContentType(_))
            file.userMeta.foreach(gfsFile.setMetaData(_))
            gfsFile.setChunkSize(file.chunkSize)
            gfsFile.getOutputStream
          })({
            os => delay { os.close }
          })({
            os => now(bytes => now(os.write(bytes.bytes, 0, bytes.n)))
          })
        }
      }
    }
  }
}
