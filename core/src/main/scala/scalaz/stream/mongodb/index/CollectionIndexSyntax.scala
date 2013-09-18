package scalaz.stream.mongodb.index

import scalaz.stream.mongodb.query.OrderPair


/** Syntax on collection indexes */
trait CollectionIndexSyntax {

  def index(h: OrderPair, t: OrderPair*): CollectionIndex = CollectionIndex((h +: t).map { case OrderPair(k, v) => (k, v) }.toMap)

  /** drops index with specified name */
  def drop(s: String): DropIndexByName = DropIndexByName(s)


  /** drops specified index from collection */
  def drop(idx: CollectionIndex): DropIndexByKeys = DropIndexByKeys(idx)


  /** Ensures index on collection, see: [[http://docs.mongodb.org/manual/reference/method/db.collection.ensureIndex/#db.collection.ensureIndex]] */
  def ensure(idx: CollectionIndex): EnsureIndex = EnsureIndex(idx)

}
