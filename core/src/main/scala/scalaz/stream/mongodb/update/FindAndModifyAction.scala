package scalaz.stream.mongodb.update

import scalaz.stream.mongodb.query._
import com.mongodb.{DBCollection, DBObject}
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task


/**
 * Action that encapsulates find and modify logic. Shall not be used directly. 
 * This action uses sort and project parameters of query to apply them before 
 * document is modified and returned. 
 *
 * @param update            Update to perform 
 * @param returnNew         Whether to return newly updated document or document before updates 
 * @param upsert            whether to perform upsert
 */
case class FindAndModifyAction(update: SimpleUpdate
                               , returnNew: Boolean = false
                               , upsert: Boolean = false) extends QueryAction[Option[DBObject]] {


  def withQuery(q: Query): ChannelResult[DBCollection,Option[DBObject]] = {
    ChannelResult {
      c =>
        Task.delay(
          Option(
            c.findAndModify(
              q.bq.o
              , q.projection.map(_.asDBObject).orNull
              , q.sort.map(_.o).orNull
              , false
              , update.asDBObject
              , returnNew
              , upsert)
          )
        )
    }
  }

  def returnNew(r: Boolean): FindAndModifyAction = copy(returnNew = r)

  def upsert(r: Boolean): FindAndModifyAction = copy(upsert = r)


}
