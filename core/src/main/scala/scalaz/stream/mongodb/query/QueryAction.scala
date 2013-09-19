package scalaz.stream.mongodb.query

import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.DBCollection

/**
 * Action that uses query. i.e. [[scalaz.stream.mongodb.update.FindAndModifyAction]] 
 */
trait QueryAction[A] {

  def withQuery(q:Query):ChannelResult[DBCollection,A]
  
}
