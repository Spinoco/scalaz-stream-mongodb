package scalaz.stream.mongodb.index


import com.mongodb.{BasicDBObjectBuilder, DBObject}

import scalaz.stream.mongodb.collectionSyntax._
import collection._

import scala.collection.JavaConverters._


/**
 * Configuration of the index. 
 * Please see [[http://docs.mongodb.org/manual/reference/method/db.collection.ensureIndex/#db.collection.ensureIndex]]
 * for explanation of the fields
 *
 */
case class CollectionIndex(keys: Map[String, Order.Value],
                           background: Option[Boolean] = None,
                           unique: Option[Boolean] = None,
                           name: Option[String] = None,
                           dropDups: Option[Boolean] = None,
                           sparse: Option[Boolean] = None,
                           expireAfterSeconds: Option[Int] = None,
                           v: Option[Int] = None,
                           weights: Option[Map[String, Int]] = None,
                           defaultLanguage: Option[String] = None,
                           languageOverride: Option[String] = None) {

  def background(b: Boolean): CollectionIndex = copy(background = Some(b))

  def unique(b: Boolean): CollectionIndex = copy(unique = Some(b))

  def name(s: String): CollectionIndex = copy(name = Some(s))

  def dropDups(b: Boolean): CollectionIndex = copy(dropDups = Some(b))

  def sparse(b: Boolean): CollectionIndex = copy(sparse = Some(b))

  def expireAfterSeconds(i: Int): CollectionIndex = copy(expireAfterSeconds = Some(i))

  def version(i: Int): CollectionIndex = copy(v = Some(i))

  def weights(h: (String, Int), t: (String, Int)*): CollectionIndex = copy(weights = Some((h +: t).toMap))

  def defaultLanguage(s: String): CollectionIndex = copy(defaultLanguage = Some(s))

  def languageOverride(s: String): CollectionIndex = copy(languageOverride = Some(s))

  lazy val optionsAsBson: DBObject = {
    BasicDBObjectBuilder.start((
                                 background.map(b => "background" -> b).toSeq ++
                                   unique.map(b => "unique" -> b) ++
                                   name.map(s => "name" -> s) ++
                                   dropDups.map(b => "dropDups" -> b) ++
                                   sparse.map(b => "sparse" -> b) ++
                                   expireAfterSeconds.map(i => "expireAfterSeconds" -> i) ++
                                   v.map(i => "v" -> i) ++
                                   weights.map(m => "weights" -> BasicDBObjectBuilder.start(m.asJava).get) ++
                                   defaultLanguage.map(s => "defaultLanguage" -> s) ++
                                   languageOverride.map(s => "languageOverride" -> s)
                                 ).toMap.asJava).get


  }

  lazy val keysAsBson: DBObject = {
    BasicDBObjectBuilder.start(
      keys.collect {
        case (k, Order.Ascending) => (k, 1)
        case (k, Order.Descending) => (k, -1)
      }.asJava).get
  }
}

