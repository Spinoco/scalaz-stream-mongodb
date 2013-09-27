package scalaz.stream.mongodb.aggregate

import scalaz.syntax.Ops
import scalaz.stream.mongodb.bson.BSONSerializable


/**
 * Snytax to be used on string for pipeline commands
 */
trait AggregationStringOps extends Ops[String] {

  private def compute(name: String, value: String, operation: String): ComputedField = ComputedField(name, value, operation)

  def addToSet(value: String): ComputedField = compute(self, value, "$addToSet")

  def first(value: String): ComputedField = compute(self, value, "$first")

  def last(value: String): ComputedField = compute(self, value, "$last")

  def max(value: String): ComputedField = compute(self, value, "$max")

  def min(value: String): ComputedField = compute(self, value, "$min")

  def avg(value: String): ComputedField = compute(self, value, "$avg")

  def push(value: String): ComputedField = compute(self, value, "$push")

  def sum(value: String): ComputedField = compute(self, value, "$sum")
  
  def setTo[A:BSONSerializable](v:A) : SetField[A] = ???
  
  def reduce(reduceF:String) : MapReduce = ???

}
