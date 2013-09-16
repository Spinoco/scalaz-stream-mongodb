package scalaz.stream.mongodb.bson

import scalaz.syntax.Ops
import scalaz.stream.mongodb.collectionSyntax._

import scala.language.higherKinds

/**
 *
 * User: pach
 * Date: 7/27/13
 * Time: 12:21 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait MapBSONOps[Map[_, _]] extends Ops[Map[_, _]] {
  def asBSON: BSONValue[Map[String, BSONAny]] = self.asInstanceOf[BSONValue[Map[String, BSONAny]]]
}
