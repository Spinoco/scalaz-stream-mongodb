package scalaz.stream.mongodb.index

import scalaz.stream.mongodb.query.OrderPair


 
/** Syntax on collection indexes */
trait CollectionIndexSyntax {

  def index(h: OrderPair, t: OrderPair*): CollectionIndex = CollectionIndex((h +: t).map { case OrderPair(k, v) => (k, v) }.toMap)

}
