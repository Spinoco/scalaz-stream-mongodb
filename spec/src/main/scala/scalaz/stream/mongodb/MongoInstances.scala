package scalaz.stream.mongodb

import scala.collection.concurrent.TrieMap

/**
 * Holds information about mongo instances used during execution of specs. Supports concurrent 
 * Specification execution
 */
object MongoInstances {

  /** Holds instances across different invocations to support concurrent instance creations */
  val instances = new TrieMap[Int, MongoInstance]()

  /**
   * Gets instance and for runtime instances will register it with registry
   * @return
   */
  def getInstance(config: MongoSpecificationConfig): MongoInstance = {

    config match {
      case runtime: MongoRuntimeConfig =>
        def go(from: Int): MongoInstance = {
          instances.get(from) match {
            case Some(present) =>
              go(from + 1)

            case None =>
              val starting = StartingMongoInstance(from)
              instances.putIfAbsent(from, starting) match {
                case Some(present) => go(from + 1)
                case None => starting.start(runtime).fold(
                  l = err => {
                    instances.remove(from)
                    throw err //just rethrow so we can force Examples not to be executed
                  }
                  , r = instance => instance
                )
              }
          }

        }
        go(runtime.bindPort)

      case instance: MongoInstanceConfig =>
        ExternalMongoInstance(instance)

    }


  }


}

