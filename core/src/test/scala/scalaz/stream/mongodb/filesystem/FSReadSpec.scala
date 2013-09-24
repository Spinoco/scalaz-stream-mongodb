package scalaz.stream.mongodb.filesystem

import org.specs2.Specification
import org.specs2.specification.Snippets
import scalaz.stream.mongodb.MongoRuntimeSpecification

import scalaz.stream.mongodb.collectionSyntax._
import org.bson.types.ObjectId
import com.mongodb.DB
import scalaz.stream.Bytes
import scalaz.concurrent.Task
import scalaz.stream.Process
import com.mongodb.gridfs.GridFS
import scala.util.Random
import org.specs2.matcher.MatchResult

import scala.language.reflectiveCalls
import scalaz.stream.mongodb.channel.ChannelResult


class FSReadSpec extends Specification with Snippets with MongoRuntimeSpecification {

  lazy val fileId = new ObjectId

  lazy val fileDb: DB = ???

  def is =


    s2"""
    
${"Reading files in filesystem".title}    
    
Files are read from the filesystem in chunks of `Bytes` that are configurable and defaults to `GridFS.DEFAULT_CHUNKSIZE` size.    
Provided `Bytes` internally recycles array of bytes read. The reading of the files is always based on first listing (or querying) 
the files in filesystem and then either applying the read operation fro all results or only for the first result returned. 


### Reading single file

Reading single file provides syntax sugar that will result in `Process[Task,Bytes]` signature once applied. 
Please see examples below:
 
* Reading single file with default buffer size : ${ snippet { list withId (fileId) and readFile() }}   ${single.read}
* Reading single file with custom buffer size  : ${ snippet { list withId (fileId) and readFile(1024) }}    ${single.readCustomChunk} 
* Reading first file from list of returned files  : ${ snippet { list withId (fileId) and readFile() }}  ${single.readFirstFrom2Files}    
        
All the above examples can be later applied to gridfs by following syntax : 
         
${ snippet {

      filesystem(fileDb) through (list withId (fileId) and readFile()): Process[Task, Bytes]

    }}         
       
       
###  Reading multiple files 
     
When reading over multiple files, instead of directly returning the process that reads all files,  Mongo Streams return 
tuple of `(MongoReadFile,Process[Task,Byte])` to eventually separate read content of files from each other. 
       
Following example reads the files starting their name with `a` and concatenates their content 
(splits chunk reads by new line), that will be separated with name of the file : 

${ snippet {

      def readFiles =
        (list files ("filename" regex "a.*") foreach (readFile())) flatMapProcess {
          case (file, reader) =>
            Process.wrap(Task.now("content of: " + file.name + "\n")) ++ reader.map(bytes => new String(bytes.toArray))

        }

      val output: Seq[String] = (filesystem(fileDb) through readFiles).collect.run

    }}
       
       
#### Additional verifications
       
* Large file is correctly split into chunks ${single.readFirstFrom2Files}       
* Empty file is correctly read ${single.readEmpty}  
* Reads multiple files ${multiple.readAndConcatenate}       
           
    """

  def is2 = multiple.readAndConcatenate


  def single = new {


    def read = ReadTest.single(list named ("2kilo") and readFile()).verify({
      case Seq(bytes) => bytes must_== defaultFiles(2)._2
      case other => ko(other.toString)
    })

    def readStandardChunk = ReadTest.single(list named ("oneMeg") and readFile()).verify({
      case bytes if bytes.size == 4 =>
        println("bytes", bytes.size, bytes.map(_.size))
        val concatenate = bytes.reduce(_ ++ _)
        (concatenate.size must_== defaultFiles(1)._2.size) and
          (concatenate must_== defaultFiles(1)._2)

      case other => ko("unexpected number of elements read")
    })

    def readCustomChunk = ReadTest.single(list named ("2kilo") and readFile(GridFS.DEFAULT_CHUNKSIZE / 2)).verify({
      case bytes if bytes.size == 2 =>
        bytes.reduce(_ ++ _) must_== defaultFiles(2)._2
      case other => ko(other.toString)
    })

    def readFirstFrom2Files = ReadTest.single(((list named ("lorem.txt")) ++ (list named ("ipsum.txt"))) and readFile(GridFS.DEFAULT_CHUNKSIZE / 2)).verify({
      case Seq(bytes) =>
        bytes must_== "Lorem".getBytes
      case other => ko(other.toString)
    })

    def readEmpty = ReadTest.single(list named ("empty") and readFile()).verify({
      case Seq() => ok
      case other => ko(other.toString)
    })

  }

  def multiple = new {

    def readAndConcatenate = ReadTest.multiple(((list named ("lorem.txt")) ++ (list named ("ipsum.txt"))).map(v => {println("$$$$", v); v }) foreach readFile()).verify({
      case Seq(lorem, ipsum) =>
        println("$$$$$$$$$$$$$$$$$$$$$$$$$", lorem, ipsum)
        (lorem must_== "lorem.txt\nLorem") and
          (ipsum must_== "ipsum.txt\nIpsum")
    })

  }


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


  case class ReadTest[A, B](cmd: ChannelResult[GridFS, A], extract: A => B) {

    val mongo = new WithMongoCollection()

    val gfs = new GridFS(mongo.db)


    def verify(f: Seq[B] => MatchResult[Any]) = {

      defaultFiles.foreach {
        case (file, content) =>
          val gfsFile = gfs.createFile(content)
          gfsFile.setFilename(file.name)
          gfsFile.save
      }


      f((filesystem(mongo.db) through cmd).map(extract).collect.run)

    }

  }

  object ReadTest {

    def single(cmd: ChannelResult[GridFS, Bytes]) = ReadTest[Bytes, Array[Byte]](cmd, b => b.toArray)

    def multiple(cmd: ChannelResult[GridFS, (MongoFileRead, Process[Task, Bytes])]) =
      ReadTest[(MongoFileRead, Process[Task, Bytes]), String](cmd, e => e._1.name + "\n" + e._2.map(b => new String(b.toArray)).collect.run.mkString)

  }

}
