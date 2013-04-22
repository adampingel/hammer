package org.pingel.hammer

import akka.actor.Cancellable
import akka.actor.Actor
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object HammerProtocol {

  case class Start(stopAfter: Option[FiniteDuration])
  case class Stop()
  case class StartNextRequest()
  case class RPS()
}

class HammerActor(lg: LoadGenerator, targetRps: Double) extends Actor {

  import HammerProtocol._

  val averageOver = 5d // seconds over which to compute average rps

  var requestSchedule: Option[Cancellable] = None

  val timestamps = collection.mutable.Set[Long]()

  def receive = {

    case Start(stopAfter) => {

      requestSchedule = Some(context.system.scheduler.schedule(
        0.millis,
        (1 / targetRps).seconds,
        self,
        StartNextRequest()))

      stopAfter map { context.system.scheduler.schedule(_, 0.seconds, self, Stop()) }

    }

    case StartNextRequest() => {
      lg.makeNextRequest() map { println(_) }
    }

    case Stop() => {
      requestSchedule.map(_.cancel)
      requestSchedule = None
      context.system.shutdown
    }

    case RPS() => sender ! (timestamps.size / 5d)
  }

}
