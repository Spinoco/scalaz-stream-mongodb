package scalaz.stream.mongodb.query

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.syntax.Ops


/**
 * Syntax to allow creation of OrderPairs from string
 */
trait OrderingOps extends Ops[String]{

  def Ascending: OrderPair = OrderPair(self,Order.Ascending)

  def Descending: OrderPair = OrderPair(self,Order.Descending)

}

