package scalaz.stream.mongodb.update

import com.mongodb.{BasicDBObject, WriteConcern}
import scala.Some
import scalaz.concurrent.Task
import scalaz.stream.mongodb.query.{Query, QueryAction}
import scalaz.stream.mongodb.channel.ChannelResult

/**
 * Removes the documents from attached query
 * @param isIsolated       Operation is performed $isolated
 * @param wc             Write concenrn if not default
 */
case class RemoveAction(isIsolated: Boolean = false, wc: Option[WriteConcern] = None) extends QueryAction[WriteResult] {


  def withQuery(q: Query): ChannelResult[WriteResult] = ChannelResult {
    c => Task.now {
      val qq =
        if (isIsolated) {
          val o = new BasicDBObject()
          o.putAll(q.bq.o)
          o.put("$isolated", 1)
          o
        } else {
          q.bq.o
        }
      WriteResult(c.remove(qq, wc.getOrElse(c.getWriteConcern)))

    }
  }

  def ensure(w: WriteConcern) = copy(wc = Some(w))

  def isolated(b: Boolean) = copy(isIsolated = b)
} 