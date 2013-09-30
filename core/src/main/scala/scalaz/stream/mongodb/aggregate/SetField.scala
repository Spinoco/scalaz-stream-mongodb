package scalaz.stream.mongodb.aggregate

import com.mongodb.DBObject


/** Sets given field to proposed value of another field */
case class SetField(key: String, value: String) extends ProjectPipelineAction
 
