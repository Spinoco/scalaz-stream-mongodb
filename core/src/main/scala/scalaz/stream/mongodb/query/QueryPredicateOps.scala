package scalaz.stream.mongodb.query

import scalaz.syntax.Ops

/** Basic syntax on Query Predicates */
trait QueryPredicateOps[A] extends Ops[A] {
  
  def and[B](qp: QueryPair[B]): BasicQuery

  def or[B](qp: QueryPair[B]): BasicQuery

  def nor[B](qp: QueryPair[B]): BasicQuery


  def and(bq: BasicQuery): BasicQuery

  def or(bq: BasicQuery): BasicQuery

  def nor(bq: BasicQuery): BasicQuery

}
