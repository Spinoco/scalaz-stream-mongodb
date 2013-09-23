package scalaz.stream.mongodb.filesystem

import org.bson.types.ObjectId
import com.mongodb.DBObject
import com.mongodb.gridfs.GridFS

/**
 *
 * User: pach
 * Date: 9/23/13
 * Time: 5:13 PM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait WriteCommandSyntax {

  /**
   * Writes to file with provided information
   * @param name        name of the file (required, must be unique) 
   * @param id          unique file identifier
   * @param meta        optional metadata to store with file 
   */
  def file(name: String, id: ObjectId = new ObjectId, meta: Option[DBObject] = None, contentType: Option[String] = None, chunkSize: Long = GridFS.DEFAULT_CHUNKSIZE): WriteCommand =
    WriteCommand(MongoFileWrite(name, id, meta, contentType, chunkSize))


}
