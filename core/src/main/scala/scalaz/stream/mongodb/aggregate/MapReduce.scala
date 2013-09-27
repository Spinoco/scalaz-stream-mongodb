package scalaz.stream.mongodb.aggregate


/**
 *
 * User: pach
 * Date: 9/26/13
 * Time: 7:47 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
case class MapReducePartial(mapFunction: String) {

  def reduce(reduceF: String): MapReduce = MapReduce(mapFunction, reduceF)

}

case class MapReduce(mapF: String
                     , reduceF: String
                     , finalizeF: Option[String] = None
                     , limit: Option[Int] = None
                     , scope: Option[String] = None
                     , jsMode: Boolean = false
                     , verboseFlag: Boolean = false) {

  def finalize(jsf: String) : MapReduce = copy(finalizeF = Some(jsf))

  def limit(count: Int) : MapReduce = copy(limit = Some(count))

  def scope(jsscope: String) : MapReduce = copy(scope = Some(jsscope))

  def jsModeInMemory : MapReduce = copy(jsMode = true)

  def verbose : MapReduce = copy(verboseFlag = true)

}
