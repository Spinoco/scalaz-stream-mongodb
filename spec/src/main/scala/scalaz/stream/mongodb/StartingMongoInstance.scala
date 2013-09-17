package scalaz.stream.mongodb

import java.io.{OutputStream, InputStream}
import scala.util.Try
import com.mongodb.MongoClient
import java.net.Socket
import scalaz.\/
import scalaz.\/._
import scala.util.Failure
import scala.Some
import scala.util.Success
import java.nio.file.{Files, Paths}
import scala.sys.process.{Process => ScalaProcess, ProcessIO}


/**
 * Instance that is just starting
 * @param port  allocated port for this local instance
 */
case class StartingMongoInstance(port: Int) extends MongoInstance {


  private[mongodb] val client = sys.error("unimplemented in this state")

  private def echo(is: InputStream, os: OutputStream) = {
    val buffer = Array.ofDim[Byte](1024)

    def go {
      val len = is.read(buffer)
      os.write(buffer, 0, len)
      if (len != -1) {
        go
      }
    }

    Try(go)
    Try(is.close)
    Try(os.close)

  }

  private def echoOs(oos: Option[OutputStream]): InputStream => Unit = {
    is =>
      oos match {
        case Some(os) => echo(is, os)
        case None => is.close
      }
  }


  private def connect(ip: String): MongoClient = {

    // Tries to open socket to mongo, it will quit once that connection is created
    // or when max no of attempts elapsed 
    def tryConnection(remains: Int, delay: Long) {
      if (remains > 0) {
        Try(new Socket(ip, port)) match {
          case Success(s) =>
            s.close()

          case Failure(f) =>
            Thread.sleep(delay)

        }
        tryConnection(remains - 1, delay)
      }
    }


    // give chance to db to warm up before we will load it 
    tryConnection(50, 200)

    new MongoClient(ip, port)

  }


  def start(runtime: MongoRuntimeConfig): Throwable \/ RunningMongoInstance = {
    //contains path to binary of mongod
    fromTryCatch {
      val mongoBin =
        (runtime.mongodPath orElse Option(System.getenv("SPEC_MONGO_HOME")).map(Paths.get(_)) orElse Option(System.getProperty("SPEC_MONGO_HOME")).map(Paths.get(_)))
        .map(v => v.resolve("bin").resolve("mongod")).filter(Files.exists(_)) getOrElse ({
          throw new Exception(s"Mongo binary cannot be resolved. Configured: ${runtime.mongodPath} from system (env SPEC_MONGO_HOME) ${sys.env.get("SPEC_MONGO_HOME")}")
        })

      val (pars, dataPath) = runtime.toCommandLinePars(Files.createTempDirectory("mongoSpec"))

      val mongod = ScalaProcess(mongoBin + " " + pars)

      val mongoProcess = mongod.run(new ProcessIO(_.close(), echoOs(runtime.mongodEchoStdOut), echoOs(runtime.mongodEchoStdErr)))

      val client = connect(runtime.bindIp)

      RunningMongoInstance(port, mongoProcess, dataPath, runtime, client)
    }
  }

  def shutdown {}

  def db(name: String) = sys.error("Not supported")
}