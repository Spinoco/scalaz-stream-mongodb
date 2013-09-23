package scalaz.stream.mongodb.filesystem

import org.bson.types.ObjectId
import com.mongodb.DBObject
import scalaz.stream.mongodb.collectionSyntax._
import java.util.Date
import com.mongodb.gridfs.GridFS


sealed trait MongoFile {

  /** unique identification of the file **/
  val id: ObjectId

}

object MongoFile {

  def apply(id: ObjectId): MongoFileRef = MongoFileRef(id)

}


/** Mongo file identified by its id **/
case class MongoFileRef(id: ObjectId) extends MongoFile


/**
 * Object holding information about the concrete file to be written
 *
 * @param name         Name of the file  
 * @param userMeta      Any associated metadata of the file.  
 * @param contentType  Eventually assigned content type to file
 */
case class MongoFileWrite(name: String
                          , id: ObjectId
                          , userMeta: Option[DBObject] = None
                          , contentType: Option[String] = None
                          , chunkSize: Long = GridFS.DEFAULT_CHUNKSIZE) extends MongoFile

/**
 * Mongo file information that are read from the filesystem
 * @param meta  MongoFile metadata
 * @param fileSystem Filesystem bucked from where this file can be read
 */
case class MongoFileRead(meta: DBObject, fileSystem: String) extends MongoFile {
  val name               = meta.as[String]("filename")
  val id                 = meta.as[ObjectId]("_id")
  val contentType        = meta.getAs[String]("contentType")
  /** length of the file in bytes **/
  val length    : Long   = meta.as[Long]("length")
  /** size of the chunks in bytes **/
  val chunkSize : Long   = meta.as[Long]("chunkSize")
  /** date when the file was uploaded to the filesystem **/
  val uploadDate: Date   = meta.as[Date]("uploadDate")
  /** md5 hash of the file **/
  val md5       : String = meta.as[String]("md5")
}
