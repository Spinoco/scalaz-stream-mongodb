package scalaz.stream.mongodb.update

import com.mongodb.{DBCollection, BasicDBObject, WriteConcern}
import scala.Some
import scalaz.stream.mongodb.query.{Query, QueryAction}
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task


/**
 * Performs update on database
 * @param update        Update action to perform
 * @param upsert        Whether to do upsert
 * @param multi
 * @param isIsolated     If the operation is isolated
 * @param wc            WriteConcern to use
 */
case class UpdateAction(update: SimpleUpdate
                        , upsert: Boolean = false
                        , multi: Boolean = false
                        , isIsolated: Boolean = false
                        , wc: Option[WriteConcern] = None) extends QueryAction[WriteResult] {


  def withQuery(q: Query): ChannelResult[DBCollection,WriteResult] = ChannelResult {
    c =>
      Task.now {
        val qq =
          if (isIsolated) {
            val o = new BasicDBObject()
            o.putAll(q.bq.o)
            o.put("$isolated", 1)
            o
          } else {
            q.bq.o
          }
        WriteResult(c.update(qq, update.asDBObject, upsert, multi, wc.getOrElse(c.getWriteConcern))) 
      }
  }

  def ensure(w: WriteConcern) = copy(wc = Some(w))

  def upsert(b: Boolean) = copy(upsert = b)

  def allDocuments(b: Boolean) = copy(multi = b)

  /**
   * When set to true, Isolates update operation. Will not work on shards and will turn off upsert functionality by default.
   * Please see [[http://www.mongodb.org/display/DOCS/Atomic+Operations]] and [[http://docs.mongodb.org/manual/reference/operator/isolated/]]
   * for more details
   */
  def isolated(b: Boolean) = copy(upsert = false, isIsolated = true)

  lazy val raw = {
    val o = new BasicDBObject()
    o.putAll(update.asDBObject)
    o
  }
}