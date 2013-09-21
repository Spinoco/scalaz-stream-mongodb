package scalaz.stream.mongodb.filesystem

import com.mongodb.{DBObject, DB}
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.concurrent.Task
import com.mongodb.gridfs.GridFS


/**
 * Represents grid fs instance 
 * @param name           Name of the bucket (default is fs)
 */
case class GridFs(name: String)  
