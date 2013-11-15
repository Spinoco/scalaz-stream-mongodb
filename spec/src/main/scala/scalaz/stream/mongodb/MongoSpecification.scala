package scalaz.stream.mongodb

import java.util.UUID
import com.mongodb.{DB, DBCollection}
import org.specs2.specification.{Step, Fragments, Scope}
import org.specs2.SpecificationLike
import java.nio.file.{Paths, Path}


/**
 * Specification, that will spawn the mongo databases for each spec instance run and shares the instance between examples
 * The mongo binary is downloaded to local filesystem if not supplied with SPEC_MONGO_HOME system environment or java defined variable
 */
trait MongoRuntimeSpecification extends MongoSpecificationBase {
  lazy val version: MongoVersion = MongoVersion("2.4.6")
  val mongoInstanceConfig: MongoSpecificationConfig = MongoRuntimeConfig()
  override lazy val instance: MongoInstance = {
    mongoInstanceConfig match {
      case runtime: MongoRuntimeConfig =>
        val path = 
        runtime.mongodPath orElse (Option(System.getenv("SPEC_MONGO_HOME")) orElse Option(System.getProperty("SPEC_MONGO_HOME"))).map(Paths.get(_)) match {
          case Some(configured) =>  configured
          case None => 
            MongoBinaryResolver.resolve(version)
        }

        MongoInstances.getInstance(runtime.copy(mongodPath = Some(path)))
        
      case other =>
        MongoInstances.getInstance(mongoInstanceConfig)
    } 
    
    
  }
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
  class WithMongoCollections(val dbName: String = randomMongoName) extends Scope {
    val db: DB = instance.db(dbName)

    /** Provides collection of given name */
    def collection(collName: String): DBCollection = db.getCollection(collName)

  }

  /**
   * Helper to create scope for only one collection 
   * @param collName  Name of the mongo collection, on randomly generated if not specified
   */
  class WithMongoCollection(val collName: String = randomMongoName) extends Scope {
    val dbName    : String       = randomMongoName
    val db        : DB           = instance.db(dbName)
    val collection: DBCollection = db.getCollection(collName)
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
      Step.stopOnFail ^
      fs ^
      Step(instance.shutdown)

}


