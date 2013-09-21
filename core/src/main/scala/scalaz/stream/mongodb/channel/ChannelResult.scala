package scalaz.stream.mongodb.channel

import scalaz.stream.Process
import scalaz.stream.Process._
import scalaz.concurrent.Task
import scalaz._
import scalaz.stream.mongodb.channel.ChannelResult


case class ChannelResult[R, A](channel: Channel[Task, R, Process[Task, A]]) {

  private[mongodb] def modify[B](f: Process[Task, A] => Process[Task, B]): ChannelResult[R, B] =
    ChannelResult(channel.map(r => r andThen (pt => pt.map(p => f(p)))))


  /** applies [[scalaz.stream.Process.map]] on resulting stream **/
  def map[B](f: A => B): ChannelResult[R, B] = modify(_.map(f))

  /** applies [[scalaz.stream.Process.flatMap]] on resulting stream **/
  def flatMapProcess[B](f: A => Process[Task, B]): ChannelResult[R, B] = modify(_.flatMap(f))

  /** binds other ChanelResult to this ChannelResult **/
  def flatMap[B](f: A => ChannelResult[R, B]): ChannelResult[R, B] = ChannelResult {
    channel.map(
      (g: R => Task[Process[Task, A]]) => (r: R) =>
        g(r).map(pa => pa.flatMap((a: A) =>
          f(a).channel.flatMap(h => wrap(h(r)).flatMap(identity))
        ))
    )
  }

  /** applies [[scalaz.stream.Process.append]] on resulting stream **/
  def append[B >: A](p2: => Process[Task, B]): ChannelResult[R, B] = modify(_.append(p2))

  /** applies [[scalaz.stream.Process.append]] on resulting stream **/
  def ++[B >: A]()(p2: => Process[Task, B]): ChannelResult[R, B] = append(p2)

  /** applies [[scalaz.stream.Process.then]] on resulting stream **/
  def fby[B >: A](p2: => Process[Task, B]): ChannelResult[R, B] = modify(_.then(p2))

  /** applies [[scalaz.stream.Process.repeat]] on resulting stream **/
  def repeat[B >: A]: ChannelResult[R, B] = modify(_.repeat)

  /** applies [[scalaz.stream.Process.kill]] on resulting stream **/
  def kill: ChannelResult[R, Nothing] = modify(_.kill)

  /** applies [[scalaz.stream.Process.killBy]] on resulting stream **/
  def killBy(e: Throwable): ChannelResult[R, Nothing] = modify(_.killBy(e))

  /** applies [[scalaz.stream.Process.causedBy]] on resulting stream **/
  def causedBy[B >: A](e: Throwable): ChannelResult[R, B] = modify(_.causedBy(e))

  /** applies [[scalaz.stream.Process.fallback]] on resulting stream **/
  def fallback: ChannelResult[R, A] = modify(_.fallback)

  /** applies [[scalaz.stream.Process.orElse]] on resulting stream **/
  def orElse[B >: A](fallback: => Process[Task, B], cleanup: => Process[Task, B] = halt): ChannelResult[R, B] = modify(_.orElse(fallback, cleanup))

  /** applies [[scalaz.stream.Process.onFailure]] on resulting stream **/
  def onFailure[B >: A](p2: => Process[Task, B]): ChannelResult[R, B] = modify(_.onFailure(p2))

  /** applies [[scalaz.stream.Process.onComplete]] on resulting stream **/
  def onComplete[B >: A](p2: => Process[Task, B]): ChannelResult[R, B] = modify(_.onComplete(p2))

  /** applies [[scalaz.stream.Process.disconnect]] on resulting stream **/
  def disconnect: ChannelResult[R, A] = modify(_.disconnect)

  /** applies [[scalaz.stream.Process.hardDisconnect]] on resulting stream **/
  def hardDisconnect: ChannelResult[R, A] = modify(_.hardDisconnect)

  /** applies [[scalaz.stream.Process.trim]] on resulting stream **/
  def trim: ChannelResult[R, A] = modify(_.trim)

  /** applies [[scalaz.stream.Process.drain]] on resulting stream **/
  def drain: ChannelResult[R, Nothing] = modify(_.drain)

  /** applies [[scalaz.stream.Process.pipe]] on resulting stream **/
  def pipe[B](p2: Process1[A, B]): ChannelResult[R, B] = modify(_.pipe(p2))

  /** applies [[scalaz.stream.Process.pipe]] on resulting stream **/
  def |>[B](p2: Process1[A, B]): ChannelResult[R, B] = pipe(p2)

  /** applies [[scalaz.stream.Process.tee]] on resulting stream **/
  def tee[B, C](p2: Process[Task, B])(t: Tee[A, B, C]): ChannelResult[R, C] = modify(_.tee(p2)(t))

  /** applies [[scalaz.stream.Process.wye]] on resulting stream **/
  def wye[B, C](p2: Process[Task, B])(y: Wye[A, B, C]): ChannelResult[R, C] = modify(_.wye(p2)(y))

  /** applies [[scalaz.stream.Process.attempt]] on resulting stream **/
  def attempt[B](f: Throwable => Process[Task, B] = (t: Throwable) => emit(Task.fail(t)).eval): ChannelResult[R, B \/ A] = modify(_.attempt(f))

  /** applies [[scalaz.stream.Process.handle]] on resulting stream **/
  def handle[B](f: PartialFunction[Throwable, Process[Task, B]]): ChannelResult[R, B] = modify(_.handle(f))

  /** applies [[scalaz.stream.Process.partialAttempt]] on resulting stream **/
  def partialAttempt[B](f: PartialFunction[Throwable, Process[Task, B]]): ChannelResult[R, B \/ A] = modify(_.partialAttempt(f))


  /** applies [[scalaz.stream.Process.zipWith]] on resulting stream **/
  def zipWith[B, C](p2: Process[Task, B])(f: (A, B) => C): ChannelResult[R, C] = modify(_.zipWith(p2)(f))

  /** zips inner processes of ChannelResults */
  def zipWith[B, C](ch2: ChannelResult[R, B])(f: (A, B) => C): ChannelResult[R, C] = ChannelResult {
    val zipper: ((R => Task[Process[Task, A]], R => Task[Process[Task, B]]) => (R => Task[Process[Task, C]])) = {
      (fa, fb) => (r: R) =>
        for {
          pa <- fa(r)
          pb <- fb(r)
        } yield (pa.zipWith(pb)(f))
    }

    channel.zipWith(ch2.channel)(zipper)
  }

  /** applies [[scalaz.stream.Process.zip]] on resulting stream **/
  def zip[B](p2: Process[Task, B]): ChannelResult[R, (A, B)] = modify(_.zip(p2))

  /** zips two channels together */
  def zip[B](ch2: ChannelResult[R, B]): ChannelResult[R, (A, B)] = zipWith(ch2)((a, b) => (a, b))

  /** applies [[scalaz.stream.Process.yipWith]] on resulting stream **/
  def yipWith[B, C](p2: Process[Task, B])(f: (A, B) => C): ChannelResult[R, C] = modify(_.yipWith(p2)(f))

  /** applies [[scalaz.stream.Process.yip]] on resulting stream **/
  def yip[B](p2: Process[Task, B]): ChannelResult[R, (A, B)] = modify(_.yip(p2))

  /** applies [[scalaz.stream.Process.merge]] on resulting stream **/
  def merge[B >: A](p2: Process[Task, B]): ChannelResult[R, B] = modify(_.wye(p2)(scalaz.stream.wye.merge))

  /** applies [[scalaz.stream.Process.either]] on resulting stream **/
  def either[B >: A, C](p2: Process[Task, C]): ChannelResult[R, B \/ C] = modify(_.wye(p2)(scalaz.stream.wye.either))

  /** applies [[scalaz.stream.Process.when]] on resulting stream **/
  def when[B >: A](condition: Process[Task, Boolean]): ChannelResult[R, B] = modify(condition.tee(_)(scalaz.stream.tee.when))

  /** applies [[scalaz.stream.Process.until]] on resulting stream **/
  def until[B >: A](condition: Process[Task, Boolean]): ChannelResult[R, B] = modify(condition.tee(_)(scalaz.stream.tee.until))

  /** applies [[scalaz.stream.Process.ProcessSyntax.through]] on resulting stream **/
  def through[B](p: Channel[Task, A, B]) = modify(_ through p)

  /** applies [[scalaz.stream.Process.ProcessSyntax.to]] on resulting stream **/
  def to(p: Sink[Task, A]) = modify(_ to p)


}

object ChannelResult {

  /** Helper to wrap simple tasks in channel result **/
  def apply[R, A](f: R => Task[A]): ChannelResult[R, A] =
    ChannelResult(wrap(Task.now((res: R) => Task.now(wrap(f(res))))))


}

