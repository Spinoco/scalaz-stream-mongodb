package scalaz.stream.mongodb.userguide

import org.specs2.Specification
import scalaz.stream.mongodb.query.{CollectionQuerySpec, QueryBuilderSpec}
import scalaz.stream.mongodb.update.{CollectionRemoveSpec, InsertAndSaveSpec, CollectionFindAndModifySpec, UpdateBuilderSpec}
import scalaz.stream.mongodb.index.{CollectionEnsureIndexSpec, CollectionDropIndexSpec, IndexBuilderSpec}
import scalaz.stream.mongodb.bson.BSONSpec


class UserGuide extends Specification {

  def is = s2"""
      
${"Mongo Streams User Guide".title}
       
This library offers wrapper over standard mongodb java driver to work with scalaz-stream library. 


${link(new BasicUsageSpec())}    
${link(new QueryBuilderSpec())}
${link(new CollectionQuerySpec())}              
${link(new UpdateBuilderSpec())}   
${link(new CollectionFindAndModifySpec())}               
${link(new InsertAndSaveSpec())}  
${link(new CollectionRemoveSpec())} 
${link(new IndexBuilderSpec())}      
${link(new CollectionEnsureIndexSpec())} 
${link(new CollectionDropIndexSpec())}  
${link(new BSONSpec())} 
              
"""

}
