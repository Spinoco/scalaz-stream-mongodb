package scalaz.stream.mongodb

import scalaz.stream.mongodb.query.{QueryEnums, QuerySyntax}
import scalaz.stream.mongodb.bson.{BSONValuesImplicits, BSONValues}
import scalaz.stream.mongodb.index.CollectionIndexSyntax
import com.mongodb.{DB, DBCollection}
import scalaz.concurrent.Task
import scalaz.stream.{Bytes, Process}
import scalaz.stream.Process._

import scala.language.implicitConversions
import scalaz.stream.mongodb.update.{UpdateSyntax, FindAndModifySyntax}
import scalaz.syntax.monad._

trait Collection {

  //todo: This is TBD when we would decide how to implement javascript
  type JavaScript = String


  implicit def dbCollection2Process(c: DBCollection): Process[Task, DBCollection] = emit(Task.now(c)).eval

  def use(c: DBCollection): Process[Task, DBCollection] = emit(Task.now(c)).eval

  implicit class DBCollectionSyntax(c: DBCollection) {
    def through[A](f: Channel[Task, DBCollection, Process[Task, A]]): Process[Task, A] =  
      (wrap(Task.now(c)) through f).join
    

    def >>>[A](f: Channel[Task, DBCollection, Process[Task, A]]): Process[Task, A] = through(f)
  }

  implicit class DBSyntax(d: DB) {

    def through[A](f: Channel[Task, DB, Process[Task, A]]): Process[Task, A] =  
      (wrap(Task.now(d)) through f).join
   

    def >>>[A](f: Channel[Task, DB, Process[Task, A]]): Process[Task, A] = through(f)

    def using[A](f: Channel[Task, DB, Process[Task, Bytes => Task[Unit]]]): Process[Task, Bytes => Task[Unit]] = through(f)
  }


}


/**
 * Generic implicit that has to be injected to get or collection related functionality in scope
 */

object collectionSyntax extends Collection
                                with QuerySyntax with QueryEnums
                                with CollectionIndexSyntax
                                with channel.ChannelResultSyntax
                                with UpdateSyntax with FindAndModifySyntax
                                with BSONValues with BSONValuesImplicits
 


