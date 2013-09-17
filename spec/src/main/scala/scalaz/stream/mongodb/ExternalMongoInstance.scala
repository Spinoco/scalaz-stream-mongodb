package scalaz.stream.mongodb

import com.mongodb.MongoClient
import java.util.UUID


/**
 * External mongo instance. We just wipe out temporary databases created once the spec is done
 * @param config
 */
case class ExternalMongoInstance(config: MongoInstanceConfig) extends MongoInstance {
  private[mongodb] val client = new MongoClient(config.bindIp, config.bindPort)

  //we need to keep track of names for later removal. This is just simple var now
  val dbNamesRequested = collection.mutable.HashSet[String]()


  def db(name: String) = {
    val realName =
      if (config.uniqueName) {
        config.prefix + name
      } else {
        config.prefix + UUID.randomUUID().toString.replaceAll("^\\d", "") + name
      }

    dbNamesRequested += realName

    client.getDB(realName)
  }

  def shutdown {
    if (config.dropDatabases) dbNamesRequested.foreach { dbn => client.dropDatabase(dbn) }
    client.close()
  }
}