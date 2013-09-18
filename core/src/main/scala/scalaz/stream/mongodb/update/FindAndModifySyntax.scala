package scalaz.stream.mongodb.update

import com.mongodb.DBObject



trait FindAndModifySyntax {

  def updateOne(ps: UpdatePair*): FindAndModifyAction = FindAndModifyAction(PairSimpleUpdate(ps))

  def updateOne(o: DBObject): FindAndModifyAction = FindAndModifyAction(ReplaceDocument(o))

  def removeOne: FindAndRemoveAction = FindAndRemoveAction()

}
