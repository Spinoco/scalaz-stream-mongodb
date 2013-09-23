package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.collectionSyntax._
import org.bson.types.ObjectId
import scalaz.stream.mongodb.query.{BasicQuery, Query, QueryPair}
import com.mongodb.DBObject

/**
 *
 * User: pach
 * Date: 9/23/13
 * Time: 4:59 PM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait ListCommandSyntax {

  /**
   * Creates the query, that uniquely identifies the single file to be read from database    
   */
  def named(name: String): ListCommand = ListCommand(FileQuery(query("filename" === name)))

  /**
   * Creates the query, that uniquely identifies the single file to be read from database    
   */
  def withId(id: ObjectId): ListCommand = ListCommand(FileQuery(query("_id" === id)))


  /**
   * Creates the query, that identifies multiple files in filesystem with metadata query
   */
  def files[A](q: QueryPair[A]*): ListCommand = ListCommand(FileQuery(Query(BasicQuery(q: _*))))

  /**
   * Creates the query, that identifies multiple files in filesystem with metadata query
   */
  def files(bq: BasicQuery): ListCommand = ListCommand(FileQuery(Query(bq)))

  /**
   * Creates the query, that identifies multiple files in filesystem with metadata query
   */
  def files(o: DBObject): ListCommand = ListCommand(FileQuery(Query(BasicQuery(o))))

}
