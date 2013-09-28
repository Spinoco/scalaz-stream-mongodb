package scalaz.stream.mongodb.aggregate

import com.mongodb.DBCollection


trait MapReduceDefinition

case class MapReducePartial(mapFunction: String) {

  def reduce(reduceF: String): MapReduce = MapReduce(mapFunction, reduceF)

}

case class MapReduce(mapF: String
                     , reduceF: String
                     , finalizeF: Option[String] = None
                     , limit: Option[Int] = None
                     , scope: Option[String] = None
                     , jsMode: Boolean = false
                     , verboseFlag: Boolean = false) extends MapReduceDefinition {

  def finalize(jsf: String): MapReduce = copy(finalizeF = Some(jsf))

  def limit(count: Int): MapReduce = copy(limit = Some(count))

  def scope(jsscope: String): MapReduce = copy(scope = Some(jsscope))

  def jsModeInMemory: MapReduce = copy(jsMode = true)

  def verbose: MapReduce = copy(verboseFlag = true)

  def persist(into: String): MapReducePersistent = MapReducePersistent(this, into)

  def persist(into: DBCollection): MapReducePersistent = MapReducePersistent(this, into.getFullName)

}

object MapReducePersistOption extends Enumeration {
  val Override, Merge, Reduce = Value
}

case class MapReducePersistent(mr: MapReduce
                               , persist: String
                               , current: Option[MapReducePersistOption.Value] = None
                               , sh: Boolean = false
                               , na: Boolean = false) extends MapReduceDefinition {

  def finalize(jsf: String): MapReducePersistent = copy(mr = mr.copy(finalizeF = Some(jsf)))

  def limit(count: Int): MapReducePersistent = copy(mr = mr.copy(limit = Some(count)))

  def scope(jsscope: String): MapReducePersistent = copy(mr = mr.copy(scope = Some(jsscope)))

  def jsModeInMemory: MapReducePersistent = copy(mr = mr.copy(jsMode = true))

  def verbose: MapReducePersistent = copy(mr = mr.copy(verboseFlag = true))

  def overrideCurrent: MapReducePersistent = copy(current = Some(MapReducePersistOption.Override))

  def mergeCurrent: MapReducePersistent = copy(current = Some(MapReducePersistOption.Merge))

  def reduceCurrent: MapReducePersistent = copy(current = Some(MapReducePersistOption.Reduce))

  def asShard: MapReducePersistent = copy(sh = true)

  def nonAtomic: MapReducePersistent = copy(na = true)

}
