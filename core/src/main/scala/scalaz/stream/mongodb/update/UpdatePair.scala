package scalaz.stream.mongodb.update

import com.mongodb.{DBObject, BasicDBObject}

import collection.JavaConverters._
import scalaz.stream.mongodb.query.{OrderPair, QuerySort}


/** Simple update pair **/
sealed trait UpdatePair {
  val op: String

  private[mongodb] def append(to: BasicDBObject)
}

/** Updates single value with given operation **/
case class ValueUpdatePair[A](op: String, k: String, v: A) extends UpdatePair {
  private[mongodb] def append(to: BasicDBObject) = to.append(k, v)
}

/** updates the whole object **/
case class DBObjectUpdatePair(op: String, v: DBObject) extends UpdatePair {
  //bit of hack to overwrite upper key
  private[mongodb] def append(to: BasicDBObject) = v.keySet().asScala.foreach(k => to.put(k, v.get(k)))
}

/** updated the array component **/
case class ArrayUpdatePair[A](op: String, k: String, v: TraversableOnce[A]) extends UpdatePair {
  private[mongodb] def append(to: BasicDBObject) = to.append(k, v.toSeq.asJava)
}

/** Appends single array */
case class ArrayAppendPair[A](op: String, k: String, v: TraversableOnce[A], sl: Option[Int] = None, srt: Option[QuerySort] = None) extends UpdatePair {
  def slice(i: Int) = copy(sl = Some(-i))

  def sort(op: OrderPair*) = copy(srt = Some(QuerySort(op)))

  private[mongodb] def append(to: BasicDBObject) = {
    if (v.size == 0) {
      sys.error("Must have at least one element") //todo: verify if this is really the case...
    } else if (v.size == 1 && sl.isEmpty && srt.isEmpty) {
      to.append(k, v.toSeq.asJava)
    } else {
      val o1 = new BasicDBObject()
      o1.append("$each", v.toSeq.asJava)
      srt.foreach { s => o1.append("$sort", s.o) }
      sl.foreach { s => o1.append("$slice", s) }
      to.append(k, o1)
    }
  }
}

/** bitwise update of value **/
case class BitwiseUpdatePair(op: String, k: String, v: Int, bitOp: String) extends UpdatePair {
  private[mongodb] def append(to: BasicDBObject) = {
    val o1 = new BasicDBObject
    o1.append(bitOp, v)
    to.append(k, o1)
  }
}