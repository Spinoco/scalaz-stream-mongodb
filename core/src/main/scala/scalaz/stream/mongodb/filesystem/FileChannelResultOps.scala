package scalaz.stream.mongodb.filesystem

import scalaz.syntax.Ops
import scalaz.stream.mongodb.channel.ChannelResult
import com.mongodb.DB
import scalaz.stream.{Bytes, Process}
import scalaz.stream.processes._
import scalaz.concurrent.Task
import com.mongodb.gridfs.GridFS


/**
 * Ops to be available for list() command
 */
trait FileChannelResultOps extends Ops[ChannelResult[DB, MongoFileRead]] {
  
  /** reads all files listed **/
  def readFiles(buffSize:Int = GridFS.DEFAULT_CHUNKSIZE): ChannelResult[DB, (MongoFileRead, Process[Task, Bytes])] = 
    self flatMap (file=>FileUtil.singleFileReader(buffSize).map(f=>(file,f(file))))

  /** reads very first file listed **/
  def readFile(buffSize:Int = GridFS.DEFAULT_CHUNKSIZE): ChannelResult[DB, Bytes] = {
    (self |> take(1))
    .flatMap(file=>FileUtil.singleFileReader(buffSize).map(f=>f(file)))
    .flatMapProcess(p => p)  //todo: join?
  }

  /** deletes very first file listed. **/
  def deleteFile: ChannelResult[DB, Unit] = {
    (self |> take(1))
    .flatMap(file=>FileUtil.singleFileRemove.map(f=>f(file)))
    .flatMapProcess(p => p) //todo: join 
  }

  
  /** deletes all files listed **/
  def deleteFiles: ChannelResult[DB, (MongoFileRead,Unit)] = {
    val zipped = self.zipWith(FileUtil.singleFileRemove){
      (file,removeAction) =>
        val rp = removeAction(file)
        rp.map(result=>(file,result))
    }
    zipped.flatMapProcess(v=>v) // todo: join?
  }
 
  
}
