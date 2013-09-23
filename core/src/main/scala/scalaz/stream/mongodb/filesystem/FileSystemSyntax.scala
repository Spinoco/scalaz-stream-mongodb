package scalaz.stream.mongodb.filesystem

import com.mongodb.{DB, DBObject}

import scala.language.implicitConversions
import scala.language.postfixOps
import com.mongodb.gridfs.GridFS
import org.bson.types.ObjectId

import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.stream.Process
import scalaz.concurrent.Task

trait FileSystemSyntax extends FileUtil {


  /** filesystem syntax, wrapper around gridFs **/
  def filesystem(db: DB, filesystemName: String = "fs"): GridFs = GridFs(db, filesystemName)

  val list: ListCommandSyntax = new ListCommandSyntax {}

  val write: WriteCommandSyntax = new WriteCommandSyntax {}


  /**
   * Creates information that uniquely identifies single file in filesystem
   * @param name        name of the file (required, must be unique) 
   * @param id          unique file identifier
   * @param meta        optional metadata to store with file 
   */
  def file(name: String, id: ObjectId = new ObjectId, meta: Option[DBObject] = None, contentType: Option[String] = None, chunkSize: Long = GridFS.DEFAULT_CHUNKSIZE): MongoFileWrite =
    MongoFileWrite(name, id, meta, contentType, chunkSize)


  /** conversion of listCommand to process */
  implicit def listCmd2ChannelResult(cmd: ListCommand) = cmd.toChannelResult


  /** syntax sugar on listCommand channelResult **/
  implicit class ListChannelResultSyntax(val self: ChannelResult[GridFS, MongoFileRead]) {

    def and[A](ch: ChannelResult[GridFS, MongoFileRead => Process[Task, A]]): ChannelResult[GridFS, A] =
      ListAndCommand.combine(self)(ch)

    def foreach[A](ch: ChannelResult[GridFS, MongoFileRead => Process[Task, A]]): ChannelResult[GridFS, (MongoFileRead, Process[Task, A])] =
      ListForEachCommand.combine(self)(ch)


  }

}
