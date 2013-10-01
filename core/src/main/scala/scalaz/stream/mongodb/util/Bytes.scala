package scalaz.stream.mongodb.util

import java.io.OutputStream
import java.util

/**
 * Helper class, immutable view into Array[Byte], before we will find the better option in scalaz-stream
 */
class Bytes(private[mongodb] val bytes: Array[Byte], private[stream] val n: Int) {
  def apply(i: Int) = bytes(i)

  def size: Int = n

  def toArray: Array[Byte] = util.Arrays.copyOf(bytes,n)
  
  def writeTo(os:OutputStream) : Unit = os.write(bytes,0,n)
  
  def copyTo(offset:Int, dest:Array[Byte], destOffset:Int, length:Int) : Unit = 
    Array.copy(bytes,offset,dest,destOffset,0)
  
}

object Bytes {
  val empty = Bytes(Array[Byte]())

  def apply(bytes: Array[Byte]): Bytes = new Bytes(bytes, bytes.length)
}
