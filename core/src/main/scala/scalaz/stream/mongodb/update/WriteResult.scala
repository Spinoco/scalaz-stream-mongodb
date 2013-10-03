package scalaz.stream.mongodb.update

import org.bson.types.ObjectId
import com.mongodb.DBObject
import scalaz.stream.mongodb.collectionSyntax._

/**
 * Encapsulation of mongo's write result in more scala like syntax
 */
sealed trait WriteResult {
  val n           : Int
  val errorMessage: Option[String]
  val serverUri   : Option[String]
  val ok: Boolean
}

/**
 * Result of write operation as returned when updating the document
 * @param n             documents affected 
 * @param errorMessage  Message in case the operation was not successful
 * @param serverUri     uri of server where request was executed
 */
case class UpdateWriteResult(n: Int, errorMessage: Option[String], serverUri: Option[String]) extends WriteResult {
  val ok = errorMessage.isEmpty
}

/**
 * Result of Insert Operation. 
 * @param n             documents affected 
 * @param errorMessage  Message in case the operation was not successful
 * @param serverUri     uri of server where request was executed 
 * @param document      document inserted          
 */
case class InsertWriteResult(n: Int, errorMessage: Option[String], serverUri: Option[String], document: DBObject) extends WriteResult {
  lazy val id : ObjectId = document.as[ObjectId]("_id")
  val ok = errorMessage.isEmpty
}


object WriteResult {
  def apply(r: com.mongodb.WriteResult): WriteResult =
    UpdateWriteResult(r.getN, None, Option(r.getField("serverUsed")).map(_.toString))

  def apply(r: com.mongodb.WriteResult, document: => DBObject): WriteResult =
    InsertWriteResult(r.getN, None, Option(r.getField("serverUsed")).map(_.toString), document)

}