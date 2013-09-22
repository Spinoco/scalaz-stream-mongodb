package scalaz.stream.mongodb.bson

import scalaz._


/**
 * We do a little of type  `magic` here to get all witnesses and implicits right 
 * on supported types
 */
trait BSONValues {
  
  /** Essentially and `convertible` scala type must have implicit of BSONValue to be used in queries **/
  type BSONValue[+A] = @@[A, BSONValueTag]

  /** Small trick to define `any` value of bson */
  type BSONAny = Tagged[BSONValueTag]

  /** BSON currently contains type of `null` this prepresents that type **/
  val BSONNull: BSONValue[Null] = null.asInstanceOf[BSONValue[Null]]

  
  
  /** Set has own type **/
  type BSONSet = Set[BSONAny]
  
  /** Constructor to create sets */
  object BSONSet {def apply(a: BSONAny*): BSONSet = Set[BSONAny](a: _*)}

  /** List has own type **/
  type BSONList[A] = List[BSONAny]

  /** Constructor to create lists */
  object BSONList {def apply(a: BSONAny*): List[BSONAny] = List[BSONAny](a: _*)}

  /** Just helper for Seq **/
  type BSONSeq = Seq[BSONAny]
  
  val BSONSeq = BSONList
  

  /** BSONObject is actually a Map **/
  type BSONObject = Map[String, BSONAny]

  /** Constructor to create objects */
  object BSONObject {def apply(ps: (String, BSONAny)*) : Map[String, BSONAny] = Map[String, BSONAny](ps: _*)}

}
