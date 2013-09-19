package scalaz.stream.mongodb.userguide


import org.specs2.Specification
import org.specs2.specification.Snippets
import com.mongodb.{DBCollection, WriteConcern, DBObject}
import scalaz.stream.processes._
import scalaz.stream.Process
import scalaz.stream.Process._
import java.io.File
import java.util.concurrent.ExecutorService
import scalaz.concurrent.Task

import scalaz.stream.mongodb.collectionSyntax._

class BasicUsageSpec extends Specification with Snippets {

  lazy val collection: DBCollection = ???
  lazy val users     : DBCollection = ???
  lazy val starWars     : DBCollection = ???
  lazy val collA     : DBCollection = ???
  lazy val collB     : DBCollection = ???

  lazy val document: DBObject = ???

  lazy val manyDocuments: Process[Task, DBObject] = ???


  def is = s2"""
  
${"Basic usage patterns".title}
              
              
Mongo Streams can be used in various ways. It is designed to inject actual dependency on collection as late as possible and provides 
powerful DSL with combinator syntax to allow expressive, simple to use, readable functional way to interact with mongoDB.

Any operation on collection is implemented as `ChannelResult[A]`. `A` Type parameter may be either `DBObject` when querying documents from
the collection, or `WriteResult` when performing save, insert or update. Additional types depends on each operation on collection. 
 
To start with mongo streams include the generic syntax import 
 
`import scalaz.stream.mongodb.collectionSyntax._`

this will add to scope required implicits for Mongo Streams DSL. 

### Create simple Queries

Now you can easily create simple queries. The basic directive to do so is findDocuments code): 

${ snippet { query("key1" === "123") }}

This will return `Query` object that can be used to perfrom actual query on collection like below:. 

${ snippet {

    def queryOnlyActive = query("active" === true)

    val allActiveUsers = (users through queryOnlyActive).collect.run
  }}

Note you may apply process combinators on the query *BEFORE* actually the query is combined with connection:

${ snippet {

    def queryOnlyFirst50Active = query("active" === true) |> take(50)

    val first50ActiveUsers = (users through queryOnlyFirst50Active).collect.run
  }} 

In example above we also piped (|>) result to another process, which further transformed the information from mongoDB. This 
essentially allows to combine queries and build library of your own queries that can be reused and then applied to collection
when necessary.
            
### Serializing and deserializing             

Standard output of query operation from streams mongo is DBObject from java mongo driver.
 
${ "Show the example with pickling once we will have picliking library for streams ready" ! todo }  


### Updating documents

Essentially there are two types of update operations that can be performed against mongo collection. 

* Operations that are based on query to limit first documents that will be affected
* Operations that directly modify the collection 
 
#### Query based updates

Query based updates are equivalents of mongoDB _findAndModify_, _findAndRemove_ and _update_ commands.

This sets in all documents with name == luke  their ship property to falcon:

${ snippet { query("name" -> "luke") and update("ship" := "falcon") } }
            
This removes all documents with name == joda from collection  (equivalent to remove in mongoDB):

${ snippet { query("name" -> "joda") and remove } }

This modifies first document with name == joda from collection (equivalent to findAndModify in mongodDB). In contrast with 
update you may receive the original of the document updated

${ snippet { query("name" -> "joda") and updateOne("ship" := None) } }
${ snippet { query("name" -> "joda") and updateOne("ship" := None).returnNew(true) } } 
${ snippet { query("name" -> "joda").sort("last" Ascending) and updateOne(document) } } 

This removes first document with name == jode from collection (equivalent to findAndModify with remove == true in mongoDB. In contrast with 
remove, you may receive original document that was removed, if present.

${ snippet { query("name" -> "joda") and removeOne } } 
${ snippet { query("name" -> "joda").sort("last" Ascending) and removeOne } } 
                                                                  
#### Inserting documents

New documents may be inserted in the mongoDB collection with either save or insert. 
 
${ snippet { insert(document) } }
${ snippet { save(document).ensure(WriteConcern.REPLICA_ACKNOWLEDGED) } }



### Combining actions together

All the actions in Mongo Streams can be combined together via process combinators. 
 
#### For comprehensions

Mongo streams actions are monads, therefor they can be nicely used in for comprehensions. Code below demonstrates one of
the possible usages where we first query all skywalkers and then we looking all friends of the skywalkers:
 

${ snippet {
    def findAllSkyWalkersWithFriends =
      for {
        skywalker <- query("name" -> "luke")
        friend <- query("friendOf" -> skywalker.as[String]("name"))
      } yield (skywalker, friend)

    starWars through findAllSkyWalkersWithFriends

  }}

Now this is actually excellent with actions on mongo collection, but how about if we want for example query all skywalker's and then maybe read
their blogs from internet? Then we can still combine  mongo stream with some process that fetches the blogs from internet web site like shown below:

 ${ snippet {

    def findBlog(sw: DBObject): Process[Task, String] = ???

    def skyWalkersBlogs = 
    for {
      sw <- query("name" -> "luke")
      blog <- findBlog(sw)
    } yield (sw, blog)


    starWars through skyWalkersBlogs

  }}



Another example that you may often need is that you query data from one colection and with results you want to query other collection. 
Of course, this can be also achieved again without knowing upfront on which mongo collections it will run:

${ snippet {

    def readFrom2(col1: DBCollection, col2: DBCollection): Process[Task, (DBObject, DBObject)] = {

      for {
        sw <- col1 through query("name" -> "luke") 
        ship <- col2 through query("ship" -> sw.getAs[String]("like"))
      } yield (sw, ship)

    }

    // then later somewhere in your code or in test
    (readFrom2(collA, collB)): Process[Task, (DBObject, DBObject)]


  }}

#### Process combinators

Mongo Stream actions have similar combinators like you will find on scalaz-stream processes. They allow you to reuse syntax from streams to 
further combine the actions with processes and processes with actions
  
    ${ snippet {

    //queries first all lukes and then all chewaccas
    def lukeAndChewacca =
    query("name" -> "luke") ++ query("name" -> "chewacca")

    //saves all lukes to file
    def flatFileStore: Sink[Task, DBObject] = ???
    
    (starWars through lukeAndChewacca) to flatFileStore 

    //or 
    
    def exportLukeAndChewacca2File = lukeAndChewacca to flatFileStore

    starWars through exportLukeAndChewacca2File

  }}
 """

}