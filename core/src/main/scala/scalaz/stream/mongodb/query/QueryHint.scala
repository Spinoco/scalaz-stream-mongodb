package scalaz.stream.mongodb.query



/** Hints to pick correct index with Query **/
sealed trait QueryHint

/** Query hint by name of index **/
case class QueryHintIndexName(index: String) extends QueryHint

/** Query hint to pick index that contain the specified keys **/
case class QueryHintByKey(keys: Set[String]) extends QueryHint



