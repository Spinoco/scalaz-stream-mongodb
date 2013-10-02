package scalaz.stream.mongodb

import java.nio.file.Path
import com.mongodb.MongoClient
import scala.sys.process.{Process => ScalaProcess}

case class RunningMongoInstance(port: Int, p: ScalaProcess, dataDir: Path, config: MongoRuntimeConfig, private[mongodb] val client: MongoClient) extends MongoInstance {

  //not tailrec, should not have more than one level of dirs in data
  //no symlinks shall be present, so simple to go...
  def removeDir(dirPath: Path) {
    dirPath.toFile.listFiles().foreach {
      f =>
        if (f.isDirectory) removeDir(f.toPath)
        f.delete()
    }
    dirPath.toFile.delete()
  }

  def db(name: String) = client.getDB(name)

  def shutdown {  
    client.close()
    p.destroy()
    p.exitValue()
    removeDir(dataDir)
    MongoInstances.instances.remove(port)
  }
}

