package scalaz.stream.mongodb.filesystem

import com.mongodb.gridfs.GridFS 
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.stream.Process._
import com.mongodb.DB
import scalaz.syntax.monad._
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.stream.mongodb.util.Bytes


/**
 * Represents grid fs instance 
 * @param db              Underlying database for gridfs
 * @param filesystemName  Name of gfs                    
 */
case class GridFs(db: DB, filesystemName: String = "fs") {

  lazy val gfs: GridFS = new GridFS(db, filesystemName)

  def using(ch: ChannelResult[GridFS,Bytes => Task[Unit]]): Process[Task, Bytes => Task[Unit]] =
    (repeatEval(Task.now(gfs)) through ch.channel).join

  def using(cmd: WriteCommand): Process[Task, Bytes => Task[Unit]] =
    (repeatEval(Task.now(gfs)) through cmd.toChannelResult.channel).join
  
  def through[A](cmd: GridFsCommand[A]): Process[Task, A] =
    (repeatEval(Task.now(gfs)) through cmd.toChannelResult.channel).join

  def through[A](ch: ChannelResult[GridFS,A]): Process[Task,A] =
    (repeatEval(Task.now(gfs)) through ch.channel).join

}  
