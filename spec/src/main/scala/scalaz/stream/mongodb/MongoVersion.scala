package scalaz.stream.mongodb

import java.net.URI


object MongoArchitecture extends Enumeration {
  val X86_64          = Value("x86_64")
  val I686            = Value("i686")
  val X86_64_2008plus = Value("x86_64-2008plus")
  val Default         = Value(defaultOSArch)


  def defaultOSArch = {
    val osArch = System.getProperty("os.arch")
    if (osArch.equals("i686_64") || osArch.equals("x86_64") || osArch.equals("amd64")) X86_64.toString else I686.toString
  }
}

/**
 * Version of Mongodb library to use for execution and download. 
 * @param version       Version to download.
 * @param architecture  Binary version of library. May not be available on certain OS combinations
 * @param nightly       Download nightly build of version                   
 */
case class MongoVersion(version: String , architecture: MongoArchitecture.Value = MongoArchitecture.Default, nightly: Boolean = false) {

  implicit class Regex(sc: StringContext) {
    def r = new scala.util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
   
  
  lazy val (osName,osSuffix) = System.getProperty("os.name") match {
    case "Linux" =>        ("linux","tgz")
    case r"Windows.*" =>   ("win32","zip")
    case "Mac OS X" =>     ("osx","tgz")
  }
  
  lazy val localFileDirName = s"mongodb-$osName-${architecture}-$version"  
  lazy val localFileName = s"$localFileDirName.$osSuffix"
  
  lazy val downloadPath: URI = {
    URI.create(s"http://fastdl.mongodb.org/$osName/$localFileDirName.$osSuffix")
  }

}  



