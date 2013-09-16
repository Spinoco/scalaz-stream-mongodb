package scalaz.stream.mongodb.bson

import com.mongodb.DBObject
import org.bson.types.ObjectId
import scala.collection.JavaConverters._
import java.util.Date


import scalaz.stream.mongodb.collectionSyntax._

import scala.language.implicitConversions
import scala.language.higherKinds


/**
 * Various conversions for tagged values 
 */
trait BSONValuesImplicits {


  //this tagging allows us to restrict types on collections primarily, effectively making own "AnyRef" for BSON
  implicit def boolean2BSONValue(b: Boolean): BSONValue[Boolean] = b.asInstanceOf[BSONValue[Boolean]]

  implicit def string2BSONValue(s: String): BSONValue[String] = s.asInstanceOf[BSONValue[String]]

  implicit def byte2BSONValue(b: Byte): BSONValue[Byte] = b.asInstanceOf[BSONValue[Byte]]

  implicit def short2BSONValue(s: Short): BSONValue[Short] = s.asInstanceOf[BSONValue[Short]]

  implicit def int2BSONValue(i: Int): BSONValue[Int] = i.asInstanceOf[BSONValue[Int]]

  implicit def long2BSONValue(i: Long): BSONValue[Long] = i.asInstanceOf[BSONValue[Long]]

  implicit def double2BSONValue(d: Double): BSONValue[Double] = d.asInstanceOf[BSONValue[Double]]

  implicit def float2BSONValue(f: Float): BSONValue[Float] = f.asInstanceOf[BSONValue[Float]]

  implicit def date2BSONValue(d: Date): BSONValue[Date] = d.asInstanceOf[BSONValue[Date]]

  implicit def objectId2BSONValue(id: ObjectId): BSONValue[ObjectId] = id.asInstanceOf[BSONValue[ObjectId]]

  implicit def object2BSONValue(o: DBObject): BSONValue[DBObject] = o.asInstanceOf[BSONValue[DBObject]]


  implicit def map2BSONValue(c: Map[String, BSONAny]): BSONValue[Map[String, BSONAny]] = c.asInstanceOf[BSONValue[Map[String, BSONAny]]]

  implicit def container2BSONValue(c: Iterable[BSONAny]): BSONValue[Iterable[BSONAny]] = c.asInstanceOf[BSONValue[Iterable[BSONAny]]]

  implicit def option2BSONValue[A: BSONSerializable](opt: Option[A]): BSONValue[Option[BSONAny]] = opt.map(unsafeAny2BSONValue(_)).asInstanceOf[BSONValue[Option[BSONAny]]]


  //not tailrec, 100 levels max (mongoDB limit), should be ok on stack
  def unsafeAny2BSONValue(a: Any): BSONAny = a match {
    case b: Boolean => boolean2BSONValue(b)
    case s: String => string2BSONValue(s)
    case b: Byte => byte2BSONValue(b)
    case s: Short => short2BSONValue(s)
    case i: Int => int2BSONValue(i)
    case l: Long => long2BSONValue(l)
    case d: Double => double2BSONValue(d)
    case f: Float => float2BSONValue(f)
    case d: Date => date2BSONValue(d)
    case id: ObjectId => objectId2BSONValue(id)
    case l: java.lang.Iterable[Any@unchecked] => l.asScala.map(unsafeAny2BSONValue(_)).toList
    case o: DBObject => o.keySet().asScala.map(k => (k, o.get(k).asInstanceOf[BSONAny])).toMap
    case null => BSONNull
  }

  implicit def bson2dbo(m: BSONObject): DBObject = BSONSerializable.map2DBObject(m)


  implicit class BSONCollection[C[A] <: Iterable[A], A: BSONSerializable](val self: C[A]) extends CollectionBSONOps[C]

  implicit class BSONMap[C[String, A] <: Map[String, A], A: BSONSerializable](val self: C[String, A]) extends MapBSONOps[C]

  implicit class BSONDBOConverter[A <: org.bson.BSONObject](val self: A) extends DBObjectOps[A]

}









 




