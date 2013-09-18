package scalaz.stream.mongodb.update

import com.mongodb.DBObject
import scalaz.stream.mongodb.query._
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task


/**
 * Find and modify with remove flag set == true
 */
case class FindAndRemoveAction() extends QueryAction[Option[DBObject]] {
  def withQuery(q: Query): ChannelResult[Option[DBObject]] =
    ChannelResult {
      c =>
        Task.now(
          Option(
            c.findAndModify(
              q.bq.o
              , q.projection.map(_.asDBObject).orNull
              , q.sort.map(_.o).orNull
              , true
              , null
              , false
              , false)
          )
        )
    }
} 

