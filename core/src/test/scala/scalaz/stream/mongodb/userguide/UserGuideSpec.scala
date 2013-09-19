package scalaz.stream.mongodb.userguide

import org.specs2.Specification
import scalaz.stream.mongodb.query.{CollectionQuerySpec, QueryBuilderSpec}
import scalaz.stream.mongodb.update.{CollectionRemoveSpec, InsertAndSaveSpec, CollectionFindAndModifySpec, UpdateBuilderSpec}
import scalaz.stream.mongodb.index.{CollectionEnsureIndexSpec, CollectionDropIndexSpec, IndexBuilderSpec}
import scalaz.stream.mongodb.bson.BSONSpec


class UserGuideSpec extends Specification {

  def is = s2"""
      
${"Mongo Streams User Guide".title}
       
This library offers wrapper over standard mongodb java driver to work with scalaz-stream library. 


${ "Basic Usage" ~/ new BasicUsageSpec() }
${ "Query Predicate DSL" ~/ new QueryBuilderSpec() }
${ "Query DSL" ~/ new CollectionQuerySpec() }
${ "Updating Collection" ~/ new UpdateBuilderSpec() }
${ "Find and Modify on Collection" ~/ new CollectionFindAndModifySpec() }
${ "Inserting or Saving new document" ~/ new InsertAndSaveSpec() }
${ "Removing document" ~/ new CollectionRemoveSpec() }
${ "Index DSL" ~/ new IndexBuilderSpec() }
${ "Removing Index" ~/ new CollectionEnsureIndexSpec() }
${ "Dropping Index" ~/ new CollectionDropIndexSpec() }
${ "BSON DSL" ~/ new BSONSpec() }
 
              
"""

}
