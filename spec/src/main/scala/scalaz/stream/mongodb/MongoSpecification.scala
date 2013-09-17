package scalaz.stream.mongodb

import java.util.UUID
import com.mongodb.DBCollection
import org.specs2.specification.{Step, Fragments, Scope}
import org.specs2.SpecificationLike


/**
 * Specification, that will spawn the mongo databases for each spec instance run and shares the instance between examples
 */
trait MongoRuntimeSpecification extends MongoSpecificationBase {
  val mongoInstanceConfig: MongoSpecificationConfig = MongoRuntimeConfig()
}

/**
 * Specification, that when used will connect to singel instance and for each spec will create own database that is shared
 * between examples 
 */
trait MongoInstanceSpecification extends MongoSpecificationBase {
  val mongoInstanceConfig: MongoSpecificationConfig = MongoInstanceConfig()
}


/** Base mongo specification trait.
  *
  * Please note all the resource management is done automatically if using only db() method to get database. 
  * If the direct access via `instance` or `client` properties is used and that actions will result in 
  * mutating the database content, user of this specification is responsible fro cleanup. 
  *
  * */
trait MongoSpecificationBase extends SpecificationLike {

  /**
   * Configuration of database instance
   * @return configuration of `mongod` to be used with this spec
   */
  val mongoInstanceConfig: MongoSpecificationConfig

  lazy val instance: MongoInstance = MongoInstances.getInstance(mongoInstanceConfig)


  /**
   * This enables environment, where dbName is either randomly generated or specified, 
   * and multiple collection may be retrieved by their name  
   */
  abstract class WithMongoCollections(val dbName: String = randomMongoName) extends Scope {

    /** Provides collection of given name */
    def collection(collName: String): DBCollection = instance.db(dbName).getCollection(collName)

  }

  /**
   * Helper to create scope for only one collection 
   * @param collName  Name of the mongo collection, on randomly generated if not specified
   */
  abstract class WithMongoCollection(val collName: String = randomMongoName) extends Scope {
    val dbName: String       = randomMongoName
    val coll  : DBCollection = instance.db(dbName).getCollection(collName)
  }

  /**
   * May be used to generate mongo random names 
   * @return
   */
  def randomMongoName = UUID.randomUUID().toString.replaceAll("^\\d", "")

  /**
   * Starts database, connects client and perform cleanup after specs are done 
   */
  override def map(fs: => Fragments) = 
    Step(instance) ^ 
      Step(stopOnFail = {instance.shutdown; true }) ^
      fs ^
      Step(instance.shutdown)

}


