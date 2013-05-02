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
import axle.quanta._
import HammerProtocol._

class HammerActor(lg: LoadGenerator) extends Actor with ActorLogging {

  import HammerProtocol._

  var requestSchedule: Option[Cancellable] = None

  import Frequency._
  var targetRps: Frequency.Q = 0 *: Hz

  import Time._

  val startTime = System.currentTimeMillis()
  var requestId = 0L

  val startTimes = collection.mutable.Map.empty[Long, Long]
  val completionTimes = collection.mutable.Map.empty[Long, Long]
  val latencies = collection.mutable.Map.empty[Long, Long]

  def stats(windowSize: Long) = {

    val now = System.currentTimeMillis
    val cutoff = math.max(startTime, now - windowSize)
    val denominator = now - cutoff

    val numRecentlyStarted = startTimes filter { case (k, t) => t >= cutoff } size
    val recentlyCompleted = completionTimes filter { case (k, t) => t >= cutoff } keySet

    val startRateAverage =
      if (denominator > 0)
        (1000d * numRecentlyStarted / denominator) *: Hz
      else
        0 *: Hz

    val completeRateAverage =
      if (denominator > 0)
        (1000 * recentlyCompleted.size.toDouble / denominator) *: Hz
      else
        0 *: Hz

    val latencyAverage =
      if (latencies.size > 0)
        (latencies.filterKeys(recentlyCompleted.contains(_)).values.sum.toDouble / recentlyCompleted.size) *: millisecond
      else
        0 *: millisecond

    Statistics(new DateTime(now), targetRps, startRateAverage, completeRateAverage, latencyAverage, requestId, startTimes.size - completionTimes.size)
  }

  def receive = {

    case TargetRPS(target) => {

      targetRps = target

      requestSchedule.map(_.cancel)

      if (targetRps.magnitude > 0) {
        val periodFD = (1d / (targetRps in KHz).magnitude.toDouble).millis
        requestSchedule = Some(context.system.scheduler.schedule(
          0.millis,
          periodFD,
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
      log.debug(s"request $requestId completed after $ms")
    }

    case GetStatistics(windowSize) => sender ! stats(windowSize)

    case PrintStatistics(windowSize) => log.info(stats(windowSize).toString)

  }

}
