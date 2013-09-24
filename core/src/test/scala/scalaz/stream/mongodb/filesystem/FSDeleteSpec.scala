package scalaz.stream.mongodb.filesystem

import org.specs2.Specification
import org.specs2.specification.Snippets
import scalaz.stream.mongodb.MongoRuntimeSpecification

import scalaz.stream.mongodb.collectionSyntax._
import com.mongodb.gridfs.GridFS
import scala.util.Random
import collection.JavaConverters._

import scala.language.reflectiveCalls


class FSDeleteSpec extends Specification with Snippets with MongoRuntimeSpecification {
  def is = s2"""
    
${"Removing files to filesystem".title}       
    
Mongo streams allows to remove files based on previsously llisted files, in very simillar way how the reading of the files work.
Instead of returning content of the file, it will return `Unit`.


Files can be removed with either single file syntax or multiple file syntax: 


* to remove single file use: ${snippet { list named ("old_file.txt") and removeFile }}    ${remove.single}
* to remove multiple files use: ${snippet { (list files() foreach removeFile).flatMapProcess(_._2) }}  ${remove.multiple}             
               
       
     """

  def is2 = remove.multiple

  def randomArray(size: Int): Array[Byte] = {
    val a = Array.ofDim[Byte](size)
    Random.nextBytes(a)
    a
  }
  
  val defaultFiles = Seq(
    file("empty") -> Array.empty[Byte]
    , file("oneMeg") -> randomArray(1024 * 1024)
    , file("2kilo") -> randomArray(GridFS.DEFAULT_CHUNKSIZE)
    , file("one") -> randomArray(1)
    , file("lorem.txt") -> "Lorem".getBytes
    , file("ipsum.txt") -> "Ipsum".getBytes
  )
  
  
  def remove = new {
    val mongo = new WithMongoCollection()

    val gfs = new GridFS(mongo.db)
    
    defaultFiles.foreach {
      case (file, content) =>
        val gfsFile = gfs.createFile(content)
        gfsFile.setFilename(file.name)
        gfsFile.save
    }
    
    def single = {
      val before = gfs.find(BSONObject()).asScala
      
      (filesystem(mongo.db) through (list named("empty") and removeFile)).run.run

      val after = gfs.find(BSONObject()).asScala
      
      (before.size must_== defaultFiles.size) and
        (after.size must_== defaultFiles.size -1) and
        (after.map(_.getFilename) must not contain("empty"))
      
    }
    
    def multiple = {
      val before = gfs.find(BSONObject()).asScala
       

      (filesystem(mongo.db) through ((list files() foreach removeFile).flatMapProcess(_._2))).run.run 

      val after = gfs.find(BSONObject()).asScala 
       
      (before.size must_== defaultFiles.size) and
        (after.size must_== 0) 


    }
    
  }
  
  
}
