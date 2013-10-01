package scalaz.stream.mongodb.filesystem

import org.specs2.Specification
import org.specs2.specification.Snippets
import scalaz.stream.mongodb.MongoRuntimeSpecification
import scalaz.stream.mongodb.collectionSyntax._
import org.bson.types.ObjectId
import com.mongodb.{DBObject, DB}
import scalaz.concurrent.Task 
import com.mongodb.gridfs.GridFS
import scalaz.stream.Process._
import scalaz.stream.processes._

import collection.JavaConverters._

import scala.language.reflectiveCalls
import java.io.InputStream
import scalaz.stream.mongodb.util.Bytes

class FSWriteSpec extends Specification with Snippets with MongoRuntimeSpecification {

  lazy val fileDb: DB = ???


  def is =
    s2"""
      
${"Writing files to filesystem".title}      
      

Mongo Streams provides syntax to build sink that writes to mongo filesystem. Syntax can define to what file data has to be 
written, what can be chunk-size and what metadata should be associated with file. 


# File syntax

File syntax creates file reference that indicates where the file has to be saved and with which data. 
Following syntax is available: 

* File named `foo` ${ snippet { file("foo") }}
* File named `foo` with supplied Id   ${ snippet { file("foo", id = new ObjectId) }}
* File named `foo` with MIME content type  ${ snippet { file("foo", contentType = Some("text/plain; charset=UTF-8")) }}   
* File named `foo` with supplied size of chunk   ${ snippet { file("foo", chunkSize = 1024) }}
* File named `foo` with supplied metadata   ${ snippet { file("foo", meta = Some(BSONObject("user" -> "luke"))) }}       
      
       
# Writing single file
       
Writing single file is easy simple syntax. Writing operation does not modify supplied Bytes object, therefore the 
Bytes object may be recycled for better efficiency

${ snippet { source10k to (filesystem(fileDb) using (write file ("foo.txt"))) }}  ${single.writeFile}
       
    """

  val source10k = repeatEval(Task.now(Bytes(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 0)))) |> take(2000)


  def extractBytes(is: InputStream, array: Array[Byte]): Array[Byte] = {
    def go(acc: Array[Byte]): Array[Byte] = {
      is.read(array) match {
        case -1 => acc
        case read => go(acc ++ array.take(read))
      }
    }

    go(Array.empty)

  }

  def single = new {

    val mongo = new WithMongoCollection()

    val gfs = new GridFS(mongo.db)

    def writeFile = {

      val fileId = new ObjectId
      val contentType = Some("text/plain; charset=UTF-8")
      val chunkSize = 2048
      val meta: Option[DBObject] = Some(BSONObject("user" -> "luke"))

      (source10k to (filesystem(mongo.db) using (write file("foo.txt", fileId, meta, contentType, chunkSize)))).run.run

      gfs.find("foo.txt").asScala.toList match {
        case gfsFile :: Nil =>
          val in = extractBytes(gfsFile.getInputStream, Array.ofDim[Byte](1024))
          (in must_== source10k.collect.run.map(_.toArray).reduce(_ ++ _)) and
            (gfsFile.getFilename must_== "foo.txt") and
            (gfsFile.getContentType must_== "text/plain; charset=UTF-8") and
            (gfsFile.getChunkSize must_== chunkSize) and
            (gfsFile.getMetaData.getAs[String]("user") must_== Some("luke"))


        case other => ko(other.toString)
      }


    }

  }


}                                             
