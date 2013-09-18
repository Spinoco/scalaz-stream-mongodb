package scalaz.stream.mongodb.update

 
trait UpdateSyntax {

  implicit class UpdatePairBuilderSyntax(val self: String) extends UpdatePairOps

}
