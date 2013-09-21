package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task
import com.mongodb.DB


trait FileSystemSyntax {

  def fileSystem(name: String = "fs"): ChannelResult[DB,Array[Byte] => Task[Unit]] = ???

}
