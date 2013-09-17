package scalaz.stream.mongodb

import java.nio.file.Path
import java.io.OutputStream


sealed trait MongoSpecificationConfig {
  val bindPort: Int
  val bindIp  : String
}

/**
 * Basic configuration class to fine-tune mongo runtime. If this configuration is used, then tests spawns own mongo instance
 * and creates databases. Instances are spawn concurrently, so it is completely safe to use specifications that are run in parallel. 
 * Examples must however be written in a way that they do not use conflicting namespaces.  
 * Please note concurrent execution may require significant amount of RAM, make sure you have few GB  
 * spare on top of your test environment. 
 *
 * Databases are than cleaned after full spec is processed and instance of mongo is teared down. 
 *
 * @param bindPort         port to bind to. Default is 27717 to not clash with any local instance that may run. 
 *                         This is initial port to use specs takes track of used ports, and will increment this for each
 *                         concurrent specs run.  Once the db is shutdown, the port is released and will be reused by
 *                         other database. 
 *                         If the Examples are run sequentially the spec guaranties the mongodb port will be always
 *                         equal to this number (so you can sniff for example on this port)
 * @param bindIp           ip to bind to, usually default is just ok
 * @param dataPath         path where data for mongo instance are stored, leave empty for system to generate temp dir and 
 *                         clean up it when needed
 * @param nssSize          size of namespace file in MB. For testing we have decreased default to low value (1 MB).
 * @param noPrealloc       by default this is false, but hence we sparing resources in specs we set this to true
 * @param cmdLinePars      other command line parameters to pass. please see `http://docs.mongodb.org/manual/reference/program/mongod/`
 *                         for possibilities
 * @param mongodPath       To run mongod from specific location this has to be set. 
 *                         If not set it is resolved automatically via querying system variable  
 *                         SPEC_MONGO_HOME that should point to mongo installation dir (parent of bin/mongod)
 * @param mongodEchoStdOut If specified, Echoes stdout output of mongod process to provided stream
 * @param mongodEchoStdErr If specified, Echoes stderr output of mongod process to provided stream                        
 *
 */
case class MongoRuntimeConfig(bindPort: Int = 27717,
                              bindIp: String = "127.0.0.1",
                              dataPath: Option[Path] = None,
                              nssSize: Int = 1,
                              noPrealloc: Boolean = true,
                              cmdLinePars: Option[String] = None,
                              mongodPath: Option[Path] = None,
                              mongodEchoStdOut: Option[OutputStream] = None,
                              mongodEchoStdErr: Option[OutputStream] = None
                               ) extends MongoSpecificationConfig {

  def toCommandLinePars(dataPathProvider: => Path): (String, Path) = {
    val dataHome = mongodPath.getOrElse(dataPathProvider)
    val pars = s"--port $bindPort --bind_ip $bindIp --dbpath $dataHome --nssize $nssSize ${if (noPrealloc) "--noprealloc" else ""} " + cmdLinePars.getOrElse("")
    (pars, dataHome)
  }

}


/**
 * If this configuration is used, mongo will connect to current database on given port and ip and will use that
 * database to perform tests. By default after all tests are done all created databases are wiped.
 * This is suitable if one cannot create for every specification instance of database. 
 *
 * @param bindPort          port on which mongo instance is running
 * @param bindIp            ip where mongo instance is running
 * @param prefix            prefix that is used always when requesting new database.  
 * @param uniqueName        When set to true, databases name is unique for each request for database. 
 *                          This avoids potential clashes of uncleaned databases, 
 *                          but may grow heavily on mongo data dir size when dropDatabases == false.
 *                          Generally this is required to be set to true, when the specs and/or examples 
 *                          are about to be run concurrently
 * @param dropDatabases     If set to true (default) will drop all databases once the tests are done. 
 *                          When set to false databases are leaved for other inspection.
 *
 */
case class MongoInstanceConfig(bindPort: Int = 27017,
                               bindIp: String = "127.0.0.1",
                               prefix: String = "spec_",
                               uniqueName: Boolean = true,
                               dropDatabases: Boolean = true) extends MongoSpecificationConfig 


 

