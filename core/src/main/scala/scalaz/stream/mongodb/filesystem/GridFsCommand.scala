package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.gridfs.GridFS


trait GridFsCommand[A] {
  def toChannelResult: ChannelResult[GridFS, A]
}

 