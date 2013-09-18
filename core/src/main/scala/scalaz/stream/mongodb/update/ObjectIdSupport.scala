package scalaz.stream.mongodb.update

import com.mongodb.DBObject
import org.bson.types.ObjectId
import scalaz.stream.mongodb.collectionSyntax._

/**
 *
 * User: pach
 * Date: 9/18/13
 * Time: 3:45 PM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait ObjectIdSupport {
  private[update] def assureId(o: DBObject): DBObject = o.getAs[ObjectId]("_id") match {
    case Some(id) => o
    case None =>
      o += ("_id" -> new ObjectId)
      o
  }
}
