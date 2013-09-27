package scalaz.stream.mongodb.aggregate

import org.specs2.Specification
import org.specs2.specification.Snippets
import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.collectionSyntax
import com.mongodb.{DBCollection, DBObject}
import scalaz.stream.mongodb.channel.ChannelResult

class PipelineSpec extends Specification with Snippets {

  def is =
    s2"""
    
    ${"Aggregation Pipeline".title}
    
    
Mongodb allows to from so called aggregation pipeline as one of the basic methods in aggregation framework.  
Mongo Streams has simple syntactic sugar for these aggregation pipeline queries and commands. 
Supported commands are: 

*     $$match   (`query` and `only` syntax)
*     $$project
*     $$limit
*     $$skip
*     $$unwind
*     $$group
*     $$sort

Below is description of syntax for every command supported


     
###  Limiting result with query and filter    

Each aggregation command starts with initial query, that is turned to very first match. For example
${snippet { query("key" -> "value") |>> limit(1) }}  will turn to aggregation command in json  
`{ "    $$match" : { "key" : "value" } }`   and  `{ "    $$limit" : 1 }`.
 
Additionally multiple queries may be chained together with `only` syntax 
(please note the `collectionSyntax` prefix is not necessary):
 
${snippet {
      query("key" -> "value") |>> (
        limit(5) |>>
          collectionSyntax.only("key2" === 3))
    }}      

Filter syntax is _converted_ to    $$match in Aggregation pipeline.        


### Grouping documents 

Basic aggregation pipeline command is `group`. Group allows to pick unique `id` for the documents that may consist
of multiple keys found in document. Then it looks for documents that have this `id` same, and when found will for 
each document compute the aggregation expression. 

${snippet {
      // creates aggregation pipeline, that first filters all documents where `key == value` then it groups all 
      // returned documents based on content of `key1` and for each document where `key1` is same will count sum of values in 
      // `key3` that is stored in `sumKey` key.
      query("key" -> "value") |>> (group("grouped" -> "$key1") compute ("sumKey" sum "$key3"))

      //multiple computations may be specified on the query: 

      (query("key" -> "value") |>> (group("grouped" -> "$key1") compute("sumKey" sum "$key3", "avgKey" avg "$key4"))): ChannelResult[DBCollection, DBObject]

    }}

There are few grouping expressions for `group` command: 
       
* sum        : ${snippet { query() |>> (group("g" -> "$k") compute ("sumKey" sum "$key3")) }}
* avg        : ${snippet { query() |>> (group("g" -> "$k") compute ("avgKey" avg "$key3")) }}  
* min        : ${snippet { query() |>> (group("g" -> "$k") compute ("minKey" min "$key3")) }}  
* max        : ${snippet { query() |>> (group("g" -> "$k") compute ("maxKey" max "$key3")) }}  
* last       : ${snippet { query() |>> (group("g" -> "$k") compute ("lastKey" last "$key3")) }}
* first      : ${snippet { query() |>> (group("g" -> "$k") compute ("firstKey" first "$key3")) }}
* addToSet   : ${snippet { query() |>> (group("g" -> "$k") compute ("setKey" addToSet "$key3")) }}
* push       : ${snippet { query() |>> (group("g" -> "$k") compute ("listKey" push "$key3")) }}

       
### Sorting documents
       
Similarly as in plain queries, the aggregation pipeline allows the sorting of the documents. This is achieved
simply via sort syntax:
${snippet {
      query("key" -> "value") |>> sort("key1" Ascending)
    }}

### Limiting and skipping documents
       
To limit results or skip unwanted results, limit and skip aggregation pipelines can be used. Following example
will skip first 10 results and then will limit the query by 20 results. 
        
${snippet {
      query("key" -> "value") |>> (skip(10) |>> limit(20))
    }}        
       
       
### Unwinding the documents

If the documents contains field, that is of list type and eventually contains document, the `unwind` syntax allows to 
convert the field to documents, that will instead of that key of list type contain each entry from the list and all other
keys of the document.

${snippet {
      query("key" -> "value") |>> unwind("key2")
    }}   
       

###  Projecting the result documents
 
Pipeline allows to transform resulting document with `project` syntax this allows you to include or exclude certian keys from document

 ${snippet {
      query("key" -> "value") |>> project("key2" include, "key3" exclude)
    }}         

Moreover there is a possibility to add whole new fields with their values (or even sub-documents) or rename the fields:
      
* renaming the fields  :   ${snippet { query() |>> (project("key2" include, "nkey2" setTo "$key2")) }}
* setting the fields to given value  :  ${snippet { query() |>> (project("key2" include, "key4" setTo 1)) }}
* setting the fields to given document  : ${snippet { query() |>> (project("key2" include, "key4" setTo BSONObject("a1" -> 1))) }}    
         
         
         
       
         
       
       """

}
