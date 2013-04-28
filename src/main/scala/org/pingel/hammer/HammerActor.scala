package org.pingel.hammer

import akka.actor.{ Actor, ActorLogging, Cancellable }
import akka.pattern._
import akka.pattern.ask
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
// import collection._
import org.joda.time.DateTime
import axle.visualize._
import axle.visualize.Plottable._
import axle.quanta.Time._
import akka.util.Timeout
import HammerProtocol._

class HammerActor(lg: LoadGenerator) extends Actor with ActorLogging {

  import HammerProtocol._

  var requestSchedule: Option[Cancellable] = None

  var targetRps = 0d
  val startTime = System.currentTimeMillis()
  var requestId = 0L
  val startTimes = collection.mutable.Map.empty[Long, Long]
  val completionTimes = collection.mutable.Map.empty[Long, Long]
  val latencies = collection.mutable.Map.empty[Long, Long]

  def stats() = {
    val now = System.currentTimeMillis

    val secondsUp = ((now - startTime) / 1000d)

    // TODO: compute averages for some rolling window (not since beginning)

    val startRateAverage = startTimes.size / secondsUp
    val completeRateAverage = completionTimes.size / secondsUp
    val latencyAverage =
      if (latencies.size > 0)
        latencies.values.sum / latencies.size
      else
        0d

    Statistics(now, targetRps, startRateAverage, completeRateAverage, latencyAverage, requestId, startTimes.size - completionTimes.size)
  }

  def receive = {

    case TargetRPS(target) => {

      targetRps = target

      if (targetRps > 0) {
        requestSchedule = Some(context.system.scheduler.schedule(
          0.millis,
          (1 / targetRps).seconds,
          self,
          StartNextRequest()))
      } else {
        requestSchedule.map(_.cancel)
        requestSchedule = None
      }

    }

    case StartNextRequest() => {

      val myId = requestId
      startTimes += myId -> System.currentTimeMillis()
      lg.makeNextRequest(myId) andThen {
        case _ => self ! RequestCompleted(myId, "TODO")
      }

      requestId += 1
    }

    case RequestCompleted(requestId, content) => {
      val time = System.currentTimeMillis()
      completionTimes += requestId -> time
      val ms = time - startTimes(requestId)
      latencies += requestId -> ms
      log.info(s"request $requestId completed after $ms milliseconds")
    }

    case GetStatistics() => sender ! stats()

    case PrintStatistics() => log.info(stats().toString)

  }

}
