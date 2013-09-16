package scalaz.stream.mongodb.bson

import scala.util.Try
import org.bson.types.ObjectId
import com.mongodb.{BasicDBObjectBuilder, DBObject}
import java.util.Date
import scala.collection.generic.CanBuildFrom
import scalaz.stream.mongodb.collectionSyntax._
import scala.collection.JavaConverters._
 

import scala.language.higherKinds
import scala.language.implicitConversions


/**
 * Instance of BSONSerializable must exists for every scala type that may be directly converted to BSON.
 * Used as witness in functions that takes BSONValues
 * @tparam A type of scala value
 */
trait BSONSerializable[A] {

  /** appends BSON compatible value to specified BSONObject. This must convert scala value to BSON counterpart **/
  def appendToBSON(o: org.bson.BSONObject, k: String, a: A): org.bson.BSONObject

  /** converts BSON value to scala type value **/
  def get(v: Any): A

  /** Safe variant of `get` **/
  def tryGet(v: Any): Try[A] = Try(get(v))

}

object BSONSerializable {

  /** Helper for any primitive scala types **/
  trait PrimitiveBSONSerializable[A] extends BSONSerializable[A] {
    def appendToBSON(o: org.bson.BSONObject, k: String, a: A): org.bson.BSONObject = {
      o.put(k, a)
      o
    }
  }

  /**
   * This is somewhat specific for boolean as we allow here the numeric and string to be implicitly converted to 
   * Boolean. 
   */
  implicit val booleanBson = new PrimitiveBSONSerializable[Boolean] {
    def get(v: Any) = v match {
      case b: Boolean => b
      case b: Byte => b >= 1
      case i: Int => i >= 1
      case l: Long => l >= 1
      case d: Double => d >= 1
      case f: Float => f >= 1
      case s: String => s.toLowerCase == "true" || Try(s.toInt >= 1).getOrElse(false)
    }


  }

  /** String conversion **/
  implicit val stringBson = new PrimitiveBSONSerializable[String] {
    def get(v: Any) = v match {
      case s: String => s
      case _ => v.toString
    }

  }

  /** Numeric type conversion **/
  implicit def numericBSON[A: Numeric] = new PrimitiveBSONSerializable[A] {
    val n = implicitly[Numeric[A]]

    //this is to make the failure early 
    def get(v: Any) = n.plus(v.asInstanceOf[A], n.zero)
  }

  /** Object Id is passed as-is **/
  implicit val objectIdBSON = new PrimitiveBSONSerializable[ObjectId] {
    def get(v: Any) = v.asInstanceOf[ObjectId]
  }

  /** DBObject is passed as-is **/
  implicit val dbObjectBSON = new PrimitiveBSONSerializable[DBObject] {
    def get(v: Any) = v.asInstanceOf[DBObject]
  }

  /** Date is passed as is. Additionally we try to recover date from long and double **/
  implicit val dateBSON = new PrimitiveBSONSerializable[Date] {
    def get(v: Any) = v match {
      case l: Long => new Date(l)
      case d: Double => new Date(d.toLong)
      case d: Date => d
    }
  }

  /** Optional fields are only added to bson when they evaluate to `Some`. This is placeholder for None **/
  implicit def noneBSON[None]: BSONSerializable[None.type] = new BSONSerializable[None.type] {
    def appendToBSON(o: org.bson.BSONObject, k: String, a: None.type) = o

    def get(v: Any) = None //should not be ever called ....
  }

  /** Optional fields are only added to bson when they evaluate to `Some`. This is extractor for Some **/
  implicit def optionBSON[A: BSONSerializable]: BSONSerializable[Option[A]] = new BSONSerializable[Option[A]] {
    lazy val ev = implicitly[BSONSerializable[A]]

    def appendToBSON(o: org.bson.BSONObject, k: String, a: Option[A]): org.bson.BSONObject = {
      a.foreach {
        va => ev.appendToBSON(o, k, va)
      }
      o
    }

    def get(v: Any) = Option(v).map(ev.get(_))
  }

  /** Map of BSONValues **/
  implicit def mapBSON[C <: Map[String, BSONAny]](implicit cbf: CanBuildFrom[Nothing, (String, BSONAny), C]): BSONSerializable[C] = new BSONSerializable[C] {
    def get(v: Any): C = v match {
      case o: DBObject =>
        val b = cbf()
        o.keySet().asScala.foreach(k => {
          val pair: (String, BSONAny) = (k, unsafeAny2BSONValue(o.get(k)))
          b += pair
        })

        b.result()
      case l: java.lang.Iterable[Any@unchecked] =>
        l.asScala.map(unsafeAny2BSONValue(_)).toList.zipWithIndex.map(e => (e._1, e._2.toString)).map(_.swap).to(cbf).toMap.asInstanceOf[C]
    }


    def appendToBSON(o: org.bson.BSONObject, k: String, a: C) = {
      o.put(k, map2DBObject(a)); o
    }

  }

  /** Another variant of Map of BSONValues **/
  implicit def mapBSON2[A: BSONSerializable](implicit cbf: CanBuildFrom[Nothing, (String, A), Map[String, A]]): BSONSerializable[Map[String, A]] = new BSONSerializable[Map[String, A]] {
    lazy val aev = implicitly[BSONSerializable[A]]

    def get(v: Any): Map[String, A] = v match {
      case o: DBObject =>
        val b = cbf()
        o.keySet().asScala.foreach(k => {
          val pair: (String, A) = (k, aev.get(o.get(k)))
          b += pair
        })

        b.result()
      case l: java.lang.Iterable[Any@unchecked] =>
        l.asScala.map(aev.get(_)).toList.zipWithIndex.map(e => (e._1, e._2.toString)).map(_.swap).to(cbf).toMap
    }


    def appendToBSON(o: org.bson.BSONObject, k: String, a: Map[String, A]) = {
      o.put(k, a.asJava); o
    }

  }

  /**
   * BSONValue iterable
   */
  implicit def iterableBSON[C <: Iterable[BSONAny]](implicit cbf: CanBuildFrom[Nothing, BSONAny, C]): BSONSerializable[C] = new BSONSerializable[C] {
    def get(v: Any): C = v match {
      case l: java.lang.Iterable[Any@unchecked] =>
        l.asScala.map(unsafeAny2BSONValue(_)).toList.to(cbf).asInstanceOf[C]
    }

    def appendToBSON(o: org.bson.BSONObject, k: String, a: C) = {
      o.put(k, iterable2jli(a)); o
    }
  }

  /** Anther variant of BSONValue iterable **/
  implicit def iterableBSON2[C[_] <: Iterable[A], A: BSONSerializable](implicit cbf: CanBuildFrom[Nothing, A, C[A]]): BSONSerializable[C[A]] = new BSONSerializable[C[A]] {
    lazy val aev = implicitly[BSONSerializable[A]]

    def appendToBSON(o: org.bson.BSONObject, k: String, a: C[A]) = {
      o.put(k, a.asJava); o
    }

    def get(v: Any): C[A] = v match {
      case l: java.lang.Iterable[Any@unchecked] =>
        l.asScala.map(aev.get(_)).toList.to(cbf)
    }
  }

  //recursive, should be ok on stack due to max bson depth of 100 
  def map2DBObject(c: Map[String, BSONAny]): DBObject = {
    val mc =
      c.collect {
        case (k, maybe: Option[BSONAny@unchecked]) if maybe.isDefined => (k, maybe.get)
        case (k, m: Map[String@unchecked, BSONAny@unchecked]) => (k, map2DBObject(m))
        case (k, it: Iterable[BSONAny@unchecked]) => (k, iterable2jli(it))
        case (k, v) if !v.isInstanceOf[Option[BSONAny@unchecked]] => (k, v) //pass through anything except None
      }

    val o = BasicDBObjectBuilder.start
    mc.foreach(e => o.add(e._1, e._2))
    o.get
  }

  //recursive, should be ok on stack due to max bson depth of 100 
  def iterable2jli(c: Iterable[BSONAny]): java.lang.Iterable[AnyRef] = {
    c.collect {
      case maybe: Option[BSONAny@unchecked] if maybe.isDefined => maybe.get
      case m: Map[String@unchecked, BSONAny@unchecked] => map2DBObject(m)
      case it: Iterable[BSONAny@unchecked] => iterable2jli(it)
      case other if !other.isInstanceOf[Option[BSONAny@unchecked]] => other //pass through anything except None
    }.asJava
  }

}
