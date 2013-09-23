package scalaz.stream.mongodb.filesystem


/** tag trait for commands that only reads from grid fs **/
trait ReadCommand[A] extends GridFsCommand[A]
