package scalaz.stream.mongodb.channel

import scalaz.stream.mongodb.MongoCollectionCommand
import scalaz.stream.Process._
import scalaz.concurrent.Task
import com.mongodb.DBCollection
import scalaz.stream.Process

import scala.language.implicitConversions

/**
 * Syntax helpers for channel
 */
trait ChannelResultSyntax {
  implicit def mongoCommand2ChannelResult[A](cmd: MongoCollectionCommand[A]): ChannelResult[DBCollection, A] = cmd.toChannelResult

  implicit def mongoCommand2Channel[A](cmd: MongoCollectionCommand[A]): Channel[Task, DBCollection, Process[Task, A]] = cmd.toChannelResult.self

  implicit def channelResult2Channel[R, A](r: ChannelResult[R, A]) = r.self
}
