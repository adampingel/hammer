package org.pingel.hammer

import akka.actor.{ Actor, ActorLogging, Cancellable }
import akka.pattern._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
import org.joda.time.DateTime
import axle.visualize._
import axle.algebra.Plottable._
import axle.quanta.Time._
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

  def stats(windowSize: Long) = {

    val now = System.currentTimeMillis
    val cutoff = math.max(startTime, now - windowSize)
    val denominator = now - cutoff

    // val secondsUp = ((now - startTime) / 1000d)
    // val startRateAverage = startTimes.size / secondsUp
    // val completeRateAverage = completionTimes.size / secondsUp
    // val latencyAverage = if (latencies.size > 0) latencies.values.sum / latencies.size else 0d

    val recentlyStarted = startTimes filter { case (k, t) => t >= cutoff } keySet
    val recentlyCompleted = completionTimes filter { case (k, t) => t >= cutoff } keySet

    val startRateAverage = if (denominator > 0) 1000 * recentlyStarted.size.toDouble / denominator else 0
    val completeRateAverage = if (denominator > 0) 1000 * recentlyCompleted.size.toDouble / denominator else 0

    val latencyAverage =
      if (latencies.size > 0)
        latencies.filterKeys(recentlyCompleted.contains(_)).values.sum.toDouble / recentlyCompleted.size
      else
        0d

    Statistics(now, targetRps, startRateAverage, completeRateAverage, latencyAverage, requestId, startTimes.size - completionTimes.size)
  }

  def receive = {

    case TargetRPS(target) => {

      targetRps = target

      requestSchedule.map(_.cancel)

      if (targetRps > 0) {
        requestSchedule = Some(context.system.scheduler.schedule(
          0.millis,
          (1 / targetRps).seconds,
          self,
          StartNextRequest()))
      } else {
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
      log.debug(s"request $requestId completed after $ms milliseconds")
    }

    case GetStatistics(windowSize) => sender ! stats(windowSize)

    case PrintStatistics(windowSize) => log.info(stats(windowSize).toString)

  }

}
