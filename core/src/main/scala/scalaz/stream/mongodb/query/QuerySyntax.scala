package scalaz.stream.mongodb.query

import com.mongodb.{DBCollection, DBObject}

import scala.language.implicitConversions

import scalaz.stream.mongodb.collectionSyntax._
import scalaz.stream.mongodb.channel.ChannelResult


/** Syntax to build up the query **/
trait QuerySyntax {

  implicit def pair2QueryPair[A: QueryPredicateWitness](p: (String, A)) = BasicQueryPair(p._1, p._2)

  implicit class QueryPairSyntax[A: QueryPredicateWitness](val p: (String, A)) extends QueryPairOps[A] {
    val self = BasicQueryPair(p._1, p._2)
  }

  implicit def pair2OrderPair(p: (String, Order.Value)) = OrderPair(p._1, p._2)

  implicit class QueryKeyPairBuilderSyntax(val self: String) extends QueryPairBuilderOps with OrderingOps with ProjectionOps

  implicit class QueryPairSyntax2[A](val self: QueryPair[A]) extends QueryPairOps[A]

  implicit def string2ProjectionPair(s: String) = SimpleProjection(s)

  implicit class BasicQuerySyntax(val self: BasicQuery) extends BasicQueryOps

  implicit def rpv2ReadPreference(rp: ReadPreference.Value) = ReadPreference(rp)

  implicit def query2ChannelResult(q:Query):ChannelResult[DBCollection,DBObject] = q.toChannelResult
  

  def query[A](q: QueryPair[A]*): Query = Query(BasicQuery(q: _*))

  def query(bq: BasicQuery): Query = Query(bq)

  def query(o: DBObject): Query = Query(BasicQuery(o))

  def not[A](p: QueryPair[A]): QueryPair[A] = p.negate

}
