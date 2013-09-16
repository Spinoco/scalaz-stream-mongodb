package scalaz.stream.mongodb.query
 
import com.mongodb.{BasicDBObjectBuilder, DBObject}
import org.specs2.Specification
import org.specs2.specification.{Example, Snippets}
import org.specs2.execute.{Snippet, SnippetParams}

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.bson._

import scala.collection.JavaConverters._
import scalaz.stream.mongodb.collectionSyntax
import Snippet._

class QueryBuilderSpec extends Specification with Snippets {
  
  lazy val extractIdx = """(\d+)\s+->\s+(.*)""".r
  lazy val examples   = makeExamples

  implicit val sp = SnippetParams[(Int, Query)]().check {
    e => {
      e._2.asDBObject.toString must_== examples(e._1)._2.toString
    }
  }


  def is = {
    val f =
      s2"""
   ${"Query DSL".title}
   
   Query DSL provides simple DSL that allows to create query that can be used in collection commands, 
   that need to limit, sort specify read preference of their operations           
   
   ##Simple Queries##
    
   ${ snippet { 1 -> query() }.verify}
   ${ snippet { 2 -> query("intKey" -> 1, "longKey" -> 2L, "doubleKey" -> 0.1d, "stringKey" -> "pat") }.verify }
   ${ snippet { 3 -> query("intKey" === 1, "longKey" ≠ 2L, "doubleKey" =/= 0.1d, "stringKey" -> "pat") }.verify }    
   ${ snippet { 4 -> query("intKey" > 1, "longKey" >= 2L, "doubleKey" < 0.1d, "intKey2" <= 33) }.verify } 
   ${ snippet { 5 -> query("intKey" within(1, 2, 3), "longKey" notWithin(2L, 3L), "doubleKey" within (5L)) }.verify }   
   ${ snippet { 6 -> query("user" all("first", "last"), "customer" all ("address")) }.verify }  
   ${ snippet { 7 -> query("user" present, "customer" present) }.verify }  
   ${ snippet { 8 -> query("userCount" %(2, 3)) }.verify }  
   ${ snippet { 9 -> query("userName" ofType BSONType.String, "customer" ofType BSONType.Object) }.verify }   
   ${ snippet { 10 -> query("user" regex ".*", "customer" regex "A.*") }.verify }  
   ${ snippet { 11 -> query("user" regex(".*", "i"), "customer" regex("A.*", "s")) }.verify }  
   Query     $$geoWithin ${todo}  
   Query     $$geoIntersects ${todo}  
   Query     $$near ${todo}  
   Query     $$nearSphere ${todo}  
   ${ snippet { 16 -> query("users" elementMatch("name" =/= "john", "age" -> 7)) }.verify }   
   ${ snippet { 17 -> query("users" elementMatch ("name" =/= "john" or "age" -> 7)) }.verify }      
   ${ snippet { 18 -> query("users" ofSize (7)) }.verify } 
   ${ snippet { 19 -> query("intKey" <= 10, "intKey" >= 3) }.verify }
   
         
         
   ##Logical Queries## 
   
   ${ snippet { 101 -> query("intKey" -> 1 and "longKey" =/= 2L) }.verify}      
   ${ snippet { 102 -> query("intKey" -> 1 and ("longKey" =/= 2L and "strKey" === "one")) }.verify}         
   ${ snippet { 103 -> query(("intKey" -> 1 and "longKey" =/= 2L) and "strKey" === "one") }.verify} 
   ${ snippet { 104 -> query(("intKey" -> 1 and "longKey" =/= 2L) and ("strKey" === "one" and "boolKey" -> true)) }.verify} 
        
   ${ snippet { 111 -> query("intKey" -> 1 or "longKey" =/= 2L) }.verify}      
   ${ snippet { 112 -> query("intKey" -> 1 or ("longKey" =/= 2L or "strKey" === "one")) }.verify}         
   ${ snippet { 113 -> query(("intKey" -> 1 or "longKey" =/= 2L) or "strKey" === "one") }.verify} 
   ${ snippet { 114 -> query(("intKey" -> 1 or "longKey" =/= 2L) or ("strKey" === "one" or "boolKey" -> true)) }.verify} 
   
   ${ snippet { 121 -> query("intKey" -> 1 nor "longKey" =/= 2L) }.verify}      
   ${ snippet { 122 -> query("intKey" -> 1 nor ("longKey" =/= 2L nor "strKey" === "one")) }.verify}         
   ${ snippet { 123 -> query(("intKey" -> 1 nor "longKey" =/= 2L) nor "strKey" === "one") }.verify} 
   ${ snippet { 124 -> query(("intKey" -> 1 nor "longKey" =/= 2L) nor ("strKey" === "one" nor "boolKey" -> true)) }.verify} 
  
   note: for the negation (not) use it without `collectionSyntax` prefix. it is here just for avoiding namespace clash in specs2      
   ${ snippet { 131 -> query(collectionSyntax.not("intKey" -> 1), "longKey" =/= 2L) }.verify}  
   ${ snippet { 132 -> query(collectionSyntax.not("intKey" >= 1), "longKey" =/= 2L) }.verify}  
        
        
   ${ snippet { 141 -> query(("intKey" -> 1 or "longKey" =/= 2L) and ("strKey" === "one" or "boolKey" -> true)) }.verify} 
   ${ snippet { 142 -> query(("intKey" -> 1 and "longKey" =/= 2L) or ("strKey" === "one" and "boolKey" -> true)) }.verify} 
  
         
         
   """

    val extractIdx = """(\d+)\s+->\s+(.*)\s+}[.]verify""".r

    f.map {
      case ex: Example =>
        ex.copy(
          desc = ex.desc.map {
            s =>
              (for (extractIdx(index, content) <- extractIdx findAllIn s) yield (index, content)).toList.headOption match {
                case Some((idx, q)) =>
                  s"${examples(idx.toInt)._1.padTo(50, ' ')} : ${markdownCode(offset = 0)(q, q).padTo(100, ' ')} ${examples(idx.toInt)._2.toString}"
                case None =>
                  s
              }
          }
        )
      case other =>
        other
    }

  }

  def makeExamples: Map[Int, (String, DBObject)] = Map(
    (1, ("Simple matchAll query", $query(dbo.get()))),
    (2, ("Multi-key match query", $query(dbo.add("intKey", 1).add("longKey", 2L).add("doubleKey", 0.1d).add("stringKey", "pat").get()))),
    (3, ("Multi-key ===, =/= query", $query(dbo.add("intKey", 1).add("longKey", dbo.add("$ne", 2L).get).add("doubleKey", dbo.add("$ne", 0.1d).get).add("stringKey", "pat").get()))),
    (4, ("Multi-key >, >=, <, <= ", $query(dbo.add("intKey", dbo.add("$gt", 1).get).add("longKey", dbo.add("$gte", 2L).get).add("doubleKey", dbo.add("$lt", 0.1d).get).add("intKey2", dbo.add("$lte", 33).get).get()))),
    (5, ("Multi-key within / notWithin query", $query(dbo.add("intKey", dbo.add("$in", Seq(1, 2, 3).asJava).get).add("longKey", dbo.add("$nin", Seq(2L, 3L).asJava).get).add("doubleKey", dbo.add("$in", Seq(5L).asJava).get).get()))),
    (6, ("Object key match", $query(dbo.add("user", dbo.add("$all", Seq("first", "last").asJava).get).add("customer", dbo.add("$all", Seq("address").asJava).get).get))),
    (7, ("Existence match", $query(dbo.add("user", dbo.add("$exists", true).get).add("customer", dbo.add("$exists", true).get).get))),
    (8, ("Modulo match", $query(dbo.add("userCount", dbo.add("$mod", Seq(2, 3).asJava).get).get))),
    (9, ("BSON Type match", $query(dbo.add("userName", dbo.add("$type", 2).get).add("customer", dbo.add("$type", 3).get).get))),
    (10, ("Regular expression match", $query(dbo.add("user", dbo.add("$regex", ".*").get).add("customer", dbo.add("$regex", "A.*").get).get))),
    (11, ("Regular expr. options match", $query(dbo.add("user", dbo.add("$regex", ".*").add("$options", "i").get).add("customer", dbo.add("$regex", "A.*").add("$options", "s").get).get))),
    (16, ("Array Element match", $query(dbo.add("users", dbo.add("$elemMatch", dbo.add("name", dbo.add("$ne", "john").get).add("age", 7).get).get).get))),
    (17, ("Array Element match logical", $query(dbo.add("users", dbo.add("$elemMatch", dbo.add("$or", Seq(dbo.add("name", dbo.add("$ne", "john").get).get, dbo.add("age", 7).get).asJava).get).get).get))),
    (18, ("Array size", $query(dbo.add("users", dbo.add("$size", 7).get).get))),
    (19, ("Same keys in query", $query($and(dbo.add("intKey", dbo.add("$lte", 10).get).get, dbo.add("intKey", dbo.add("$gte", 3).get).get)))),

    (101, ("Simple and", $query($and(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get)))),
    (102, ("Complex and I.", $query($and(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get)))),
    (103, ("Complex and II.", $query($and(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get)))),
    (104, ("Complex and III.", $query($and(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get, dbo.add("boolKey", true).get)))),

    (111, ("Simple or", $query($or(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get)))),
    (112, ("Complex or I.", $query($or(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get)))),
    (113, ("Complex or II.", $query($or(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get)))),
    (114, ("Complex or III.", $query($or(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get, dbo.add("boolKey", true).get)))),

    (121, ("Simple nor", $query($nor(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get)))),
    (122, ("Complex nor I.", $query($nor(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get)))),
    (123, ("Complex nor II.", $query($nor(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get)))),
    (124, ("Complex nor III.", $query($nor(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get, dbo.add("strKey", "one").get, dbo.add("boolKey", true).get)))),

    (131, ("Equality not", $query(dbo.add("intKey", dbo.add("$ne", 1).get).add("longKey", dbo.add("$ne", 2L).get).get))), //unfortunatelly order of simple ands is HashSet
    (132, ("Negation of expression", $query(dbo.add("intKey", dbo.add("$not", dbo.add("$gte", 1).get).get).add("longKey", dbo.add("$ne", 2L).get).get))), //unfortunatelly order of simple ands is HashSet


    (141, ("Combine and and or", $query($and($or(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get), $or(dbo.add("strKey", "one").get, dbo.add("boolKey", true).get))))),
    (142, ("Combine or and and", $query($or($and(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get), $and(dbo.add("strKey", "one").get, dbo.add("boolKey", true).get))))),


    (1010000, ("Simple and", $query($and(dbo.add("intKey", 1).get, dbo.add("longKey", dbo.add("$ne", 2L).get).get))))
  )
 
  def dbo = new BasicDBObjectBuilder

  def $query(o: DBObject) = dbo.add("$query", o).get

  def $and(o: DBObject*): DBObject = logical("$and", o)

  def $or(o: DBObject*): DBObject = logical("$or", o)

  def $nor(o: DBObject*): DBObject = logical("$nor", o)


  def logical(cmd: String, o: Seq[DBObject]) = dbo.add(cmd, o.asJava).get


}