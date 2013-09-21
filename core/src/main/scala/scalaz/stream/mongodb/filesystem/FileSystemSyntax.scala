package scalaz.stream.mongodb.filesystem

import com.mongodb.{DBObject, DB}
import scalaz.concurrent.Task

import scalaz.stream.{Bytes, Process}
import scalaz.stream.Process._

import scalaz.stream.mongodb.collectionSyntax._
import org.bson.types.ObjectId
import scalaz.stream.mongodb.query.{Query, BasicQuery, QueryPair}

import scalaz.stream.mongodb.channel.ChannelResult


import scala.language.postfixOps

trait FileSystemSyntax {

  /**
   * Creates filesystem instance. 
   * @param name         Name of the bucket, default is `fs`
   */
  def filesystem(name: String = "fs"): GridFs = GridFs(name)


  /**
   * Creates information that uniquely identifies single file in filesystem
   * @param name        name of the file (required, must be unique) 
   * @param id          unique file identifier
   * @param meta        optional metadata to store with file 
   */
  def file(name: String, id: ObjectId = new ObjectId, meta: Option[DBObject] = None, contentType: Option[String] = None): MongoFileWrite =
    MongoFileWrite(name, id, meta, contentType)

  /**
   * Creates the query, that uniquely identifies the single file to be read from database    
   */
  def named(name: String): FileQuery = FileQuery(query("filename" === name))

  /**
   * Creates the query, that uniquely identifies the single file to be read from database    
   */
  def withId(id: ObjectId): FileQuery = FileQuery(query("_id" === id))


  /**
   * Creates the query, that identifies multiple files in filesystem with metadata query
   */
  def files[A](q: QueryPair[A]*): FileQuery = FileQuery(Query(BasicQuery(q: _*)))

  /**
   * Creates the query, that identifies multiple files in filesystem with metadata query
   */
  def files(bq: BasicQuery): FileQuery = FileQuery(Query(bq))

  /**
   * Creates the query, that identifies multiple files in filesystem with metadata query
   */
  def files(o: DBObject): FileQuery = FileQuery(Query(BasicQuery(o)))


  /** Syntax for GridFs **/
  implicit class GridFsSyntax(val self: GridFs) extends GridFsOps

  /** Specific operations when channel is type of MongoFileRead */
  implicit class FileChannelResultSyntax(val self: ChannelResult[DB, MongoFileRead]) extends FileChannelResultOps

}
