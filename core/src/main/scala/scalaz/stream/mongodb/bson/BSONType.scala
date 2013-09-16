package scalaz.stream.mongodb.bson


/**
 * Helper to map proper BSON Types
 */
object BSONType extends Enumeration {
  val Double = Value(1)
  val String = Value(2)
  val Object = Value(3)
  val Array = Value(4)
  val Binary = Value(5)
  val ObjectId = Value(7)
  val Boolean = Value(8)
  val Date = Value(9)
  val Null = Value(10)
  val RegEx = Value(11)
  val JavaScript = Value(13)
  val Symbol = Value(14)
  val ScopedJavascript = Value(15)
  val Integer = Value(16)
  val TimeStamp = Value(17)
  val Long = Value(18)
  val MinKey = Value(255)
  val MaxKey = Value(127)

}
