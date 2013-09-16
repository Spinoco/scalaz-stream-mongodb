package scalaz.stream.mongodb.bson

import scalaz.syntax.Ops
import org.bson.types.ObjectId
import scala.util.Try

import scalaz.stream.mongodb.collectionSyntax._
import scala.collection.JavaConverters._


/**
 * Operations on mongodb object.
 * NOTE:
 * Unfortunately mongodb object is mutable structure. So this actually "mutates" the mongodb instead
 * of creating new instance every time. 
 * @tparam A
 */
trait DBObjectOps[A <: org.bson.BSONObject] extends Ops[A] {

  /** Helper to get a _id of the object, this is type-unsafe and may fail with exception if _id is not `ObjectId` **/
  def _id: Option[ObjectId] = getAs[ObjectId]("_id")

  /** Safe variant of `_id` **/
  def try_id: Option[Try[ObjectId]] = tryGetAs[ObjectId]("_id")

  /** Gets field of object, if of supplied type. This is type-unsafe and may fail with exception if k is not of `A` type **/
  def getAs[A: BSONSerializable](k: String): Option[A] = tryGetAs(k).map(_.get)

  /** Safe variant of `getAs` **/
  def tryGetAs[A: BSONSerializable](k: String): Option[Try[A]] =
    Option(self.get(k)).map(implicitly[BSONSerializable[A]].tryGet(_))

  /** Unsafe variant of getAs, will fail if the `k` does not exists and if `k` is different type than `A` **/ 
  def apply[A: BSONSerializable](k: String): A = tryAs[A](k).get

  /** Alias to `apply` */
  def as[A: BSONSerializable](k: String): A = apply(k)

  /** Safe variant of `as` **/
  def tryAs[A: BSONSerializable](k: String): Try[A] =
    implicitly[BSONSerializable[A]].tryGet(self.get(k))

  /** Adds given key to object **/
  def +=[A: BSONSerializable](p: (String, A)): org.bson.BSONObject =
    implicitly[BSONSerializable[A]].appendToBSON(self, p._1, p._2)

  /** appends elements of given map  to BSBObject. This mutates DBObject and will return mutated object **/
  def ++=(m: Map[String, BSONAny]): org.bson.BSONObject = {
    self.putAll(BSONSerializable.map2DBObject(m))
    self
  }

  /** appends keys and values to BSBObject. This mutates DBObject and will return mutated object **/
  def ++=(o: org.bson.BSONObject): org.bson.BSONObject = {
    self.putAll(o)
    self
  }

  /** removes given key from DBObject. This mutates DBObject and will return mutated object **/
  def -=(k: String): org.bson.BSONObject = {
    self.removeField(k)
    self
  }

  /** Removes more keys from DBObject. This mutates DBObject and will return mutated object **/
  def --=(k: TraversableOnce[String]) = {
    k.foreach(self.removeField(_))
    self
  }

  /** Converts DBObject to scala map. Returned map is immutable and will not reflect any changed in DBObject **/
  def asMap: scala.collection.mutable.Map[String, Any] = self.toMap.asScala.map {
    case (k: String, v) => (k, v)
    case (k, v) => (k.toString, v)
  }


}

