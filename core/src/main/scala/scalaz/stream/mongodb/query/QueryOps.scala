package scalaz.stream.mongodb.query

import scalaz.syntax.Ops
import scalaz.stream.mongodb.index.CollectionIndex
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.stream.mongodb.collectionSyntax._
import com.mongodb.{DBCollection, DBObject}

/**
 *
 * User: pach
 * Date: 9/19/13
 * Time: 8:00 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
trait QueryOps extends Ops[Query] {

  def where(js:String) : Query = self.copy(where = Some(js))
  
  def sort(h: OrderPair, t: OrderPair*): Query = self.copy(sort = Some(QuerySort(h +: t)))

  def orderby(h: OrderPair, t: OrderPair*): Query = sort(h, t: _*)

  def limit(max: Int): Query = self.copy(limit = Some(max))

  def skip(by: Int): Query = self.copy(skip = Some(by))

  def from(pref: ReadPreference): Query = self.copy(readPreference = Some(pref))

  def from(pref: ReadPreference.Value): Query = self.copy(readPreference = Some(ReadPreference(pref)))

  def project(h: ProjectionPair, t: ProjectionPair*): Query = self.copy(projection = Some(QueryProjection(h +: t)))

  def explain(flag: ExplainVerbosity.Value): Query = self.copy(explainFlag = Some(flag))

  def hint(n: String): Query = self.copy(hint = Some(QueryHintIndexName(n)))

  def hint(index: CollectionIndex): Query = self.copy(hint = Some(QueryHintByKey(index.keys.keySet.toSet)))

  def snapshot(b: Boolean): Query = self.copy(snapshotFlag = Some(b))

  def comment(s: String): Query = self.copy(comment = Some(s))

  /** Applies action on query result **/
  def and[A](a: QueryAction[A]): ChannelResult[DBCollection,A] = a.withQuery(self)

  /** Appends results of two queries together **/
  def append(q: Query): ChannelResult[DBCollection,DBObject] = ChannelResult {
    self.toChannelResult.channel.zipWith(q.toChannelResult.channel) {
      (qf1, qf2) => (c: DBCollection) =>
        qf1(c).flatMap(p1 => qf2(c).map(p2 => p1 ++ p2))
    }
  }

  /** alias for `append` **/
  def ++(q: Query): ChannelResult[DBCollection,DBObject] = append(q)


}
