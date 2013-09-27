package scalaz.stream.mongodb.query

import scalaz.syntax.Ops

/** Syntax to build projection pairs */
trait ProjectionOps extends Ops[String]{ 

  def exclude = SimpleProjection(self, true)
  
  def include = SimpleProjection(self)

  def $ = SimpleProjection(self + ".$")

  def first(i: Int) = SliceProjection(self, i)

  def last(i: Int) = SliceProjection(self, -i)

  def skip(from: Int) = new {
    def limit(to: Int) = SliceProjection(self, from, Some(to))
  }
}
