package scalaz.stream.mongodb.channel

import scalaz.stream.mongodb.MongoCommand
import scalaz.stream.Process._
import scalaz.concurrent.Task
import com.mongodb.DBCollection
import scalaz.stream.Process

/**
 * Syntax helpers for channel
 */
trait ChannelResultSyntax {
  implicit def mongoCommand2ChannelResult[A](cmd: MongoCommand[A]): ChannelResult[A] = cmd.toChannelResult

  implicit def mongoCommand2Channel[A](cmd: MongoCommand[A]): Channel[Task, DBCollection, Process[Task, A]] = cmd.toChannelResult.self

  implicit def channelResult2Channel[A](r: ChannelResult[A]) = r.self
}
