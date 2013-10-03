package scalaz.stream.mongodb.aggregate

import com.mongodb.{BasicDBObject, DBObject, DBCollection}
import scalaz.stream.mongodb.channel.ChannelResult
import scalaz.stream.Process
import scalaz.stream.Process._
import scalaz.concurrent.Task
import scalaz.stream.mongodb.query.Query
import collection.JavaConverters._


trait MapReduceDefinition {

  def toChannelResult(q: Query): ChannelResult[DBCollection, DBObject]

}

case class MapReduce(mapF: String
                     , reduceF: String
                     , finalizeF: Option[String] = None
                     , limit: Option[Int] = None
                     , scope: Option[String] = None
                     , jsMode: Boolean = false
                     , verboseFlag: Boolean = false) extends MapReduceDefinition {

  def finalize(jsf: String): MapReduce = copy(finalizeF = Some(jsf))

  def limit(count: Int): MapReduce = copy(limit = Some(count))

  def scope(jsscope: String): MapReduce = copy(scope = Some(jsscope))

  def jsModeInMemory: MapReduce = copy(jsMode = true)

  def verbose: MapReduce = copy(verboseFlag = true)

  def persist(into: String): MapReducePersistent = {
    val pair = into.split("[.]")  
    if (pair.length == 1) {
      MapReducePersistent(this, into)
    } else {
      MapReducePersistent(this, pair.tail.mkString("."), Some(pair.head))
    }
    
  }

  def persist(into: DBCollection): MapReducePersistent = MapReducePersistent(this, into.getName, Some(into.getDB.getName))

  def toChannelResult(q: Query): ChannelResult[DBCollection, DBObject] = ChannelResult {
    import Task._
    Process.eval[Task, DBCollection => Task[Process[Task, DBObject]]] {
      now {
        c: DBCollection => delay {
          val command = buildMrCommand(c, q)
          val executed = c.mapReduce(command)
          if (executed.getCommandResult().ok()) {
            //todo: current implementation of MR is strict in java driver maybe we can find a way how to make it lazy?
            emitAll[DBObject](executed.results().asScala.toSeq).evalMap(Task.now(_))
          } else {
            Process.fail(executed.getCommandResult.getException)
          }
        }
      }
    }

  }

  protected[aggregate] def buildMrCommand(c: DBCollection, q: Query): DBObject = {
    val cmd = new BasicDBObject
    cmd.append("mapReduce", c.getName)
    cmd.append("map", mapF)
    cmd.append("reduce", reduceF)
    cmd.append("out", new BasicDBObject().append("inline", 1))
    if (q.bq.o.keySet().size > 0) cmd.append("query", q.bq.o)
    q.sort.foreach(s => cmd.append("sort", s.o))
    limit.foreach((l => cmd.append("limit", l)))
    finalizeF.foreach(ff => cmd.append("finalize", ff))
    scope.foreach(s => cmd.append("scope", s))
    cmd.append("jsMode", jsMode)
    cmd.append("verbose", verboseFlag)
    cmd
  }

}

object MapReducePersistOption extends Enumeration {
  val Override, Merge, Reduce = Value
}

case class MapReducePersistent(mr: MapReduce
                               , persistCollection: String
                               , persistDb: Option[String]  = None
                               , current: MapReducePersistOption.Value = MapReducePersistOption.Override
                               , sh: Boolean = false
                               , na: Boolean = false) extends MapReduceDefinition {

  def finalize(jsf: String): MapReducePersistent = copy(mr = mr.copy(finalizeF = Some(jsf)))

  def limit(count: Int): MapReducePersistent = copy(mr = mr.copy(limit = Some(count)))

  def scope(jsscope: String): MapReducePersistent = copy(mr = mr.copy(scope = Some(jsscope)))

  def jsModeInMemory: MapReducePersistent = copy(mr = mr.copy(jsMode = true))

  def verbose: MapReducePersistent = copy(mr = mr.copy(verboseFlag = true))

  def mergeCurrent: MapReducePersistent = copy(current = MapReducePersistOption.Merge)

  def reduceCurrent: MapReducePersistent = copy(current = MapReducePersistOption.Reduce)

  def asShard: MapReducePersistent = copy(sh = true)

  def nonAtomic: MapReducePersistent = copy(na = true)

  def toChannelResult(q: Query): ChannelResult[DBCollection, DBObject] = ChannelResult {
    import Task._
    Process.eval[Task, DBCollection => Task[Process[Task, DBObject]]] {
      now {
        c: DBCollection => delay {
          val command = mr.buildMrCommand(c, q)
          val out = new BasicDBObject()
          current match {
            case MapReducePersistOption.Override => out.append("replace", persistCollection)
            case MapReducePersistOption.Merge =>  out.append("merge", persistCollection)
            case MapReducePersistOption.Reduce => out.append("reduce", persistCollection)
          }
          persistDb.foreach(out.append("db",_))
          if (sh) out.append("sharded",true)
          if (na) out.append("nonAtomic", true)
          command.put("out",out)
          val executed = c.mapReduce(command)
          if (executed.getCommandResult().ok()) {
            //todo: current implementation of MR is strict in java driver maybe we can find a way how to make it lazy?
            emitAll[DBObject](executed.results().asScala.toSeq).evalMap(Task.now(_))
          } else {
            Process.fail(executed.getCommandResult.getException)
          }
        }
      }
    }

  }
}
