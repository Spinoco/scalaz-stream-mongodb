package scalaz.stream.mongodb

/**
 *
 * User: pach
 * Date: 10/5/13
 * Time: 7:58 AM
 * (c) 2011-2013 Spinoco Czech Republic, a.s.
 */
object DownloadApp extends App{

  MongoBinaryResolver.resolve(MongoVersion("2.4.6"))
  
  
}
