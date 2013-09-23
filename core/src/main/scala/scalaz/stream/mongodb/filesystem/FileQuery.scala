package scalaz.stream.mongodb.filesystem

import scalaz.stream.mongodb.query.{OrderPair, Query}

import scala.language.postfixOps


/**
 * Query that allows to pick multiple files
 * @param q
 */
case class FileQuery(q: Query) {

  def sort(h: OrderPair, t: OrderPair*): FileQuery = copy(q = q.sort(h, t: _*))

  def orderby(h: OrderPair, t: OrderPair*): FileQuery = sort(h, t: _*)

}
 
