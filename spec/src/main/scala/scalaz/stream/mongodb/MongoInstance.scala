package scalaz.stream.mongodb

import com.mongodb.{DB, MongoClient}


/** Abstracts over various mongo instance environments the specs can be run within.  **/
trait MongoInstance {
  private[mongodb] val client: MongoClient

  def shutdown: Unit

  /**
   * Gets 
   * @param name
   * @return
   */
  def db(name: String): DB
}



