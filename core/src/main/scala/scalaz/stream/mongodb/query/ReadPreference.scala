package scalaz.stream.mongodb.query
 


import scala.language.implicitConversions
import com.mongodb.BasicDBObject


/**
 * mongodb ReadPreference constructor Helper 
 */
case class ReadPreference(pref: ReadPreference.Value, preferred: Boolean = false, tag: List[(String, String)] = Nil) {

  import com.mongodb.{ReadPreference => MRP}

  implicit def pair2TagSetDBO(pair: (String, String)) = new BasicDBObject().append(pair._1, pair._2)

  implicit def seqPair2TagSeqDBO(pair: Seq[(String, String)]) = pair.map(pair2TagSetDBO(_))

  def preferred(b: Boolean): ReadPreference = copy(preferred = true)

  def tags(h: (String, String), t: (String, String)*): ReadPreference = copy(tag = (h +: t).toList)

  private[mongodb] val asMongoDbReadPreference: com.mongodb.ReadPreference = this match {
    case ReadPreference(ReadPreference.Nearest, _, Nil) => MRP.nearest()
    case ReadPreference(ReadPreference.Nearest, _, h :: Nil) => MRP.nearest(h)
    case ReadPreference(ReadPreference.Nearest, _, h :: t) => MRP.nearest(h, t: _*)
    case ReadPreference(ReadPreference.Primary, true, Nil) => MRP.primaryPreferred()
    case ReadPreference(ReadPreference.Primary, true, h :: Nil) => MRP.primaryPreferred(h)
    case ReadPreference(ReadPreference.Primary, true, h :: t) => MRP.primaryPreferred(h, t: _*)
    case ReadPreference(ReadPreference.Primary, false, Nil) => MRP.primary()
    case ReadPreference(ReadPreference.Primary, false, nonEmpty) => sys.error("not supported")
    case ReadPreference(ReadPreference.Secondary, true, Nil) => MRP.secondaryPreferred()
    case ReadPreference(ReadPreference.Secondary, true, h :: Nil) => MRP.secondaryPreferred(h)
    case ReadPreference(ReadPreference.Secondary, true, h :: t) => MRP.secondaryPreferred(h, t: _*)
    case ReadPreference(ReadPreference.Secondary, false, Nil) => MRP.secondary()
    case ReadPreference(ReadPreference.Secondary, false, h :: Nil) => MRP.secondary(h)
    case ReadPreference(ReadPreference.Secondary, false, h :: t) => MRP.secondary(h, t: _*)
  }

}

object ReadPreference extends Enumeration {
  val Primary, Secondary, Nearest = Value
}
