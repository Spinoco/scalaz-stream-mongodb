package scalaz.stream.mongodb.query

 
/** Enumerations used in Query definition **/
trait QueryEnums {

  /** Enumeration to define explain verbosity **/
  object ExplainVerbosity extends Enumeration {
    val Normal, Verbose = Value
  }


  /** Enumeration to define ordering **/
  object Order extends Enumeration {
    val Ascending = Value(1)
    val Descending = Value(-1)
  }

}
