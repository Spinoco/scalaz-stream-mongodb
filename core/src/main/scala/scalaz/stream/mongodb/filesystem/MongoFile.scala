package scalaz.stream.mongodb.filesystem

import org.bson.types.ObjectId
import com.mongodb.{BasicDBObject, DBObject}
import scalaz.stream.mongodb.collectionSyntax._
import java.util.Date


sealed trait MongoFile {

  /** name of the file, must not be unique **/
  val name       : String
  /** unique identification of the file **/
  val id         : ObjectId
  /** content type of file **/
  val contentType: Option[String]
  /** metadata of file **/
  val meta       : DBObject


}

/**
 * Object holding information about the concrete file to be written
 *
 * @param name         Name of the file  
 * @param userMeta      Any associated metadata of the file.  
 * @param contentType  Eventually assigned content type to file
 */
case class MongoFileWrite(name: String, id:ObjectId,  userMeta: Option[DBObject], contentType: Option[String]) extends MongoFile {
  
  lazy val meta: DBObject = {
    val o = new BasicDBObject()
    o.append("filename", name)
    o.append("_id", id)
    contentType.foreach(o.append("contentType", _))
    userMeta.foreach(o.putAll(_))
    o
  }
}

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
