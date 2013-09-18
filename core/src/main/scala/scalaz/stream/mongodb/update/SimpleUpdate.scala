package scalaz.stream.mongodb.update

import com.mongodb.{BasicDBObject, DBObject}


trait SimpleUpdate {
  val asDBObject:DBObject
}

/** Generate update object from update pairs */
case class PairSimpleUpdate(pairs: Seq[UpdatePair]) extends SimpleUpdate {
  lazy val asDBObject: DBObject = {
    val o = new BasicDBObject()
    pairs.groupBy(_.op).foreach {
      case (op, ps) =>
        val o1 = new BasicDBObject
        ps.foreach {  up =>   up.append(o1) }
        o.put(op, o1)
    }
    o
  }
}
 
/** Just simple replace of the document **/
case class ReplaceDocument(o:DBObject) extends SimpleUpdate{
  val asDBObject = o
}
