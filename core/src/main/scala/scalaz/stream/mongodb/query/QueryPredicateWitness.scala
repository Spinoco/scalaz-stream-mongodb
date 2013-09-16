package scalaz.stream.mongodb.query


/**
 * Witness for allowed predicates in query
 * @tparam A
 */
trait QueryPredicateWitness[A]

object QueryPredicateWitness {

  implicit val boolWitness = new QueryPredicateWitness[Boolean] {}

  implicit val stringWitness = new QueryPredicateWitness[String] {}

  implicit def optionWitness[A: QueryPredicateWitness] = new QueryPredicateWitness[Option[A]] {}

  implicit val intWitness = new QueryPredicateWitness[Int] {}

  implicit val longWitness = new QueryPredicateWitness[Long] {}

  implicit val doubleWitness = new QueryPredicateWitness[Double] {}


}
