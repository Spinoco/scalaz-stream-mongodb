package scalaz.stream.mongodb

import scalaz.stream.mongodb.query.{QueryEnums, QuerySyntax}
import scalaz.stream.mongodb.bson.{BSONValuesImplicits, BSONValues}
import scalaz.stream.mongodb.index.CollectionIndexSyntax


trait Collection {

  //todo: This is TBD when we would decide how to implement javascript
  type JavaScript = String


}


/**
 * Generic implicit that has to be injected to get or collection related functionality in scope
 */

object collectionSyntax extends Collection
                                with QuerySyntax with QueryEnums
                                with CollectionIndexSyntax
                                with BSONValues with BSONValuesImplicits
 


