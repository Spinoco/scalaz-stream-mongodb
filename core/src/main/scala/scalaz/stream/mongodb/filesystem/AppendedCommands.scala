package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.gridfs.GridFS

/**
 * Command that appends two gridfs commands together
 */
case class AppendedCommands[A](cmd1: GridFsCommand[A], cmd2: GridFsCommand[A]) extends GridFsCommand[A] {
  def toChannelResult: ChannelResult[GridFS, A] = cmd1.toChannelResult ++ cmd2.toChannelResult

  def append(other: GridFsCommand[A]): AppendedCommands[A] = AppendedCommands(this, other)

  def ++(other: GridFsCommand[A]): AppendedCommands[A] = append(other)
}
