package scalaz.stream.mongodb.update


import com.mongodb.DBObject
import scalaz.stream.mongodb.query.{QueryPair, BasicQuery}


trait UpdateSyntax {

  /** implicit for findAndModify */
  implicit class UpdatePairBuilderSyntax(val self: String) extends UpdatePairOps

  /** santax creating update action from pairs */
  def update(ps: UpdatePair*): UpdateAction = UpdateAction(PairSimpleUpdate(ps))

  /** syntax creating update action that replaces the whole document */
  def update(o: DBObject): UpdateAction = UpdateAction(ReplaceDocument(o))

  /** syntax for saving supplied uobject */
  def save(o: DBObject): SaveAction = SaveAction(o)

  /** syntax for inserting supplied object */
  def insert(o: DBObject): InsertAction = InsertAction(o)

  /** syntax for removing documents */
  def remove: RemoveAction = RemoveAction()

  /** specific syntax for array update operations **/
  def pull[A](qp: QueryPair[A]*): DBObjectUpdatePair = DBObjectUpdatePair("$pull", BasicQuery(qp: _*).o)

}
