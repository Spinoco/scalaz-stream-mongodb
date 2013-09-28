package scalaz.stream.mongodb.aggregate

import org.specs2.Specification
import org.specs2.specification.Snippets

import scalaz.stream.mongodb.collectionSyntax._
import com.mongodb.DBCollection

/**
 *
 * User: pach
 * Date: 9/28/13
 * Time: 4:31 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
class MapReduceSpec extends Specification with Snippets {

  lazy val mapJavaScript: String = ???

  lazy val reduceJavaScript: String = ???

  lazy val finalizeJavaScript: String = ???

  lazy val scopeJavascript: String = ???

  lazy val dbCollection : DBCollection = ???

  def is =
    s2"""

${"MapReduce aggregation".title}      
      
Mongo Streams supports mongodb`s mapreduce functionality. Similarly with Aggregation Pipeline syntax mapreduce is based
on initial query. It also honors the `sort` syntax from query. Following are available syntaxes for mapreduce operation:

* simple map and reduce operation ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript) }} 
* with finaliaztion function  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript finalize finalizeJavaScript) }}
* with sorting of documents first   ${ snippet { query() sort ("key" Ascending) mapReduce (mapJavaScript reduce reduceJavaScript finalize finalizeJavaScript) }}      
* with configuring javascript scope   ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript scope scopeJavascript) }}                      
* with configuring in-memory javascript object passing  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript jsModeInMemory) }}           
* with configuring increased verbosity  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript verbose) }}            
* with specifying maximum number of results returned ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript limit(100)) }}


### Specifying ouput of mapreduce command

Each mapreduce command may eventually specify if results will be stored persistently into database, where these results will be stored and 
how the results will be stored. This is controlled with persists syntax:


 * in current database under given collection : ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist("results")) }}    
 * in supplied collection and database :    ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist("resultdb.results"))  }}  
 * in supplied collection  :    ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist(dbCollection))  }}   
 * to replace current records in result collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist(dbCollection) overrideCurrent)  }}         
 * to merge with current records in result collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist(dbCollection) mergeCurrent)  }}   
 * to reduce with current records in result collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist(dbCollection) reduceCurrent)  }}   
 * to store results in sharded collection :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist(dbCollection) asShard)  }} 
 * to make the store operation nonatomic :  ${ snippet { query() mapReduce (mapJavaScript reduce reduceJavaScript persist(dbCollection) nonAtomic)  }}   

     """
}
