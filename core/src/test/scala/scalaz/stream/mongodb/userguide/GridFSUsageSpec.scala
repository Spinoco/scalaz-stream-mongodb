package scalaz.stream.mongodb.userguide

import org.specs2.Specification
import org.specs2.specification.Snippets
import com.mongodb.DB
import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.Process
import scalaz.concurrent.Task
import org.bson.types.ObjectId
import java.util.Date
import scalaz.stream.processes._
import scalaz.syntax.monad._
import java.io.InputStream
import scalaz.stream.mongodb.filesystem._ 
import scalaz.stream.mongodb.filesystem.MongoFileRead
import scalaz.stream.mongodb.util.Bytes

/**
 *
 * User: pach
 * Date: 9/21/13
 * Time: 1:57 PM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
class GridFSUsageSpec extends Specification with Snippets {

  lazy val filesDb: DB = ???

  def is = s2"""

${"Mongodb filesystem usage".title} 
             
Mongo streams allow simple usage of mongodb GridFS.  Files can be listed, read, written and combined with 
other scalaz-stream processes.

Similarly with collection queries, the binding of GridFS to actual database where the files are stored is 
delayed up to the very last time. This allows for great reuse of code and nice abstraction.
       
### Filesystem
            
Initial construct of Mongo Streams is the `filesystem` construct. It references to mongodb`s filesystem (or bucket). 
Default name of this filesystem is `fs`. To specify filesystem following constructs are available : 

${ snippet {

    val defaultFilesystem = filesystem(filesDb)

    val namedFileSystem = filesystem(filesDb, filesystemName = "fsName")

  }}
            
            
### Listing files            

Basic operation on mongodb is to list files in filesystem. To list files
in filesystem use `list()` directive as shown below:

${ snippet {

    val listAllFiles = list files()


    val listAllFilesNamedGoo = list named ("goo")

    //running the queries 

    (filesystem(filesDb) through listAllFiles): Process[Task, MongoFileRead]
    (filesystem(filesDb) through listAllFilesNamedGoo): Process[Task, MongoFileRead]

    //combining the results 

    val listEverything = listAllFiles ++ listAllFilesNamedGoo

    //then running them

    (filesystem(filesDb) through listEverything): Process[Task, MongoFileRead]

  }}
             
List command supports additional queries to select particular files with similar syntax as Mongo Streams query() : 
           
${ snippet {

    val fileId: ObjectId = ???


    val filesMatchingFoo = list named ("foo")
    val filesWithSuppliedId = list withId (fileId)

    //complex queries 

    val filesWithRegex = list files ("filename" regex "report.*")
    val olderFiles = list files ("uploadDate" <= new Date)


  }}        
     
Please see more in ${link(new FSListSpec)}.     


### Reading from files 
              
Reading is an action on the returned list of files. There are two possible variants of reading. Files are either 
read directly (first file matching the list) or process with result of reading the files is returned to allow for 
reading multiple files:
               
${ snippet {
    val fileId: ObjectId = ???

    val readSingleFile = list withId (fileId) and readFile()

    val readAllFiles = list files() foreach readFile()

    //run the reads  
    (filesystem(filesDb) through readSingleFile): Process[Task, Bytes]

    //run the multiple Files, please not the different type
    val allFiles = ((filesystem(filesDb)) through readAllFiles): Process[Task, (MongoFileRead, Process[Task, Bytes])]

    //now to read every file that has size >= 100 bytes and concatenate their output
    ((allFiles |> filter { case (file, _) => file.length > 100 }).map(v => v._2).join): Process[Task, Bytes]


  }}               
              
Please note that read operation recycles the inner array object on every read attempt. The object is encapsulated 
within `scalaz.stream.Bytes` primitive and underlying array must not be mutated. It is safe however to call at 
any time `toArray` method that will allocate `Array[Byte]`.
              
              
If there is need to specify custom size of read buffer (default is == `com.mongodb.gridfs.GridFS.DEFAULT_CHUNKSIZE`) 
you can pass this to `read` command as argument:

${snippet {

    //specify read buffer of 1024 bytes
    val customRead = filesystem(filesDb) through (list files() and readFile(1024))


  }}
         
Please see more in ${link(new FSReadSpec)}.   
              
### Writing to files 
              
In order to write file to filesystem, the Mongo Streams library builds the Sink, that can be used to write any source of 
Bytes. File to which data will be written needs to be specified with `file()` command : 

${snippet {

    //creates file named `foo.txt` with supplied ObjectId. Please note `id` must be unique in given filesystem
    file("foo.txt", id = new ObjectId)

    //creates file named `foo.txt` with metadata setting `owner` key to `luke` value
    file("foo.txt", meta = Some(BSONObject("owner" -> "luke")))


  }}

To write into such created file simply use the write syntax

${snippet {

    val fileId = new ObjectId

    //write to single file
    val writeToFile = write file("foo.txt", id = fileId)

    val fileSource: InputStream = ???
    val readBuffer = new Array[Byte](1024 * 1024)

    //read from one file (inputstream) and write to single file 
    (Process.eval(Task.now(readBuffer)) through unsafeChunkR(fileSource)).map(Bytes(_)) to (filesystem(filesDb) using writeToFile)

  }}

Please see more in ${link(new FSWriteSpec)}.   

### Deleting files
              
Files can be removed from filesystem by using delete command. 
              
${ snippet {

    // deletes all files that matches the query
    filesystem(filesDb) through (list named ("old") and removeFile)

    // provides process that allows to run delete for each file in query
    filesystem(filesDb) through (list named ("old") foreach removeFile)

  }}
               
Please see more in ${link(new FSDeleteSpec)}.       
      
"""


}
