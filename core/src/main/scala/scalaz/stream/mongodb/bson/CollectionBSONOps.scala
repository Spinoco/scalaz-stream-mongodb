package scalaz.stream.mongodb.bson

import scalaz.syntax.Ops

import scalaz.stream.mongodb.collectionSyntax._

import scala.language.higherKinds

/**
 *
 * User: pach
 * Date: 7/27/13
 * Time: 12:22 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait CollectionBSONOps[C[_] <: Iterable[_]] extends Ops[C[_]] {
  def asBSON: BSONValue[C[BSONAny]] = self.asInstanceOf[BSONValue[C[BSONAny]]]
}
