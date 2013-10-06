package scalaz.stream.mongodb

import java.nio.file._
import java.io.{BufferedOutputStream, OutputStream}

/**
 * Helper object to resolve Mongo binaries either from local cache path (~/.streams.mongodb, or from internet
 * This actually run only in situation, when the MongoRuntimeConfig is used 
 */
object MongoBinaryResolver {

  lazy val cachePath = Paths.get(System.getProperty("user.home")).resolve(".streams.mongodb")

  


  /**
   * Resolves path to mongodb binary. If needed binary is downloaded
   * @param version
   * @return
   */
  def resolve(version: MongoVersion): Path = synchronized {
    val localBinDir = cachePath.resolve(version.localFileDirName) 
    if (Files.exists(localBinDir)) {
      localBinDir
    } else {
      println("Mongo binary not present, starting download")
      extract(version,download(version)) 
    }
  }

  /**
   * Downloads specified version from the net
   * @param version
   * @return
   */
  def download(version: MongoVersion): Path = {

    val destination = cachePath.resolve(version.localFileName)
    val is = version.downloadPath.toURL.openStream()
    if (Files.exists(destination)) Files.delete(destination)
    val os = Files.newOutputStream(destination)

    val dotEvery = (512 * 1024) // 0.5mb

    try {
      Files.createDirectories(cachePath)
      println("Downloading the binary of mongo from " + version.downloadPath)

      println("Downloaded binary will be stored to " + destination)
   

      val buff = Array.ofDim[Byte](100*1024) //100k buff
      def go(read:Long) : Long = {
         val i = is.read(buff)
         if ( i >= 0) {
           os.write(buff,0,i)
           val total = read + i
           // println(total, dotEvery, total % dotEvery < read % dotEvery)
           if (total % dotEvery < read % dotEvery) print(".")
           go(total)
         } else {
           os.flush()
           read
         }
      }
      val copied = go(0)
      println() //lf only
 
      println(s"Downloaded $copied bytes")
      cachePath.resolve(version.localFileName)
    } catch {
      case t: Throwable =>
        println("!!!!! DOWNLOAD FAILURE " + t.getMessage)
        t.printStackTrace()
        throw t
    } finally {
      os.close
      is.close
    }

  }
  
  def extract(version:MongoVersion,archive:Path) : Path = {
    import scala.sys.process._
    import scala.language.postfixOps
    
    version.osName match {
      case "linux" => s"tar xf $archive  -C $cachePath" !
      case "win32" => sys.error("not implemented yet")
      case "osx" =>   s"tar xf $archive  -C $cachePath" !
    }
    
   
    println(s"Extracted to ${cachePath.resolve(version.localFileDirName)}")  
    Files.delete(archive)
    cachePath.resolve(version.localFileDirName)
  }


}
