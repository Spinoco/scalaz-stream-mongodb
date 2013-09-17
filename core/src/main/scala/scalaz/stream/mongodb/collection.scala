package scalaz.stream.mongodb

import scalaz.stream.mongodb.query.{QueryEnums, QuerySyntax}
import scalaz.stream.mongodb.bson.{BSONValuesImplicits, BSONValues}
import scalaz.stream.mongodb.index.CollectionIndexSyntax
import com.mongodb.DBCollection
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.stream.Process._

import scala.language.implicitConversions

trait Collection {

  //todo: This is TBD when we would decide how to implement javascript
  type JavaScript = String


  implicit def dbCollection2Process(c:DBCollection):Process[Task,DBCollection] = emit(Task.now(c)).eval
  
  def use(c:DBCollection):Process[Task,DBCollection] = emit(Task.now(c)).eval

  implicit class DBCollectionSyntax(c:DBCollection) {
    def through[A](f: Channel[Task,DBCollection,Process[Task,A]]): Process[Task,A] = {
      (emit(Task.now(c)).eval through f).flatMap(p=>p)
    } 
    
    def >>>[A](f: Channel[Task,DBCollection,Process[Task,A]]):Process[Task,A] = through(f) 
  }
  
  
  
}


/**
 * Generic implicit that has to be injected to get or collection related functionality in scope
 */

object collectionSyntax extends Collection
                                with QuerySyntax with QueryEnums
                                with CollectionIndexSyntax
                                with channel.ChannelResultSyntax
                                with BSONValues with BSONValuesImplicits
 


