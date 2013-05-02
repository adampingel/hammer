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

  // Note: The following 3 maps will grow without bound during execution:

  // The keys are all the request ids, which start at 0L and decrement from there
  // This keeps allows the natural Long sort order to put the most recent requests at the
  // head of the sorted map

  var startTimes = collection.immutable.TreeMap.empty[Long, Long]
  var requestsOrderedByCompletionTime = List.empty[(Long, Long)]
  var latencies = collection.immutable.TreeMap.empty[Long, Long]

  def stats(windowSize: Long) = {

    val now = System.currentTimeMillis
    val cutoff = math.max(startTime, now - windowSize)
    val denominator = now - cutoff

    val numRecentlyStarted = startTimes takeWhile { case (id, t) => t >= cutoff } size
    val recentlyCompleted = requestsOrderedByCompletionTime takeWhile { case (id, t) => t >= cutoff } map { _._1 }

    val startRateAverage =
      if (denominator > 0)
        (1000d * numRecentlyStarted / denominator) *: Hz
      else
        0 *: Hz

    val completeRateAverage =
      if (denominator > 0)
        (1000d * recentlyCompleted.size / denominator) *: Hz
      else
        0 *: Hz

    val latencyAverage =
      if (latencies.size > 0) {
        (recentlyCompleted.map(latencies(_)).sum.toDouble / recentlyCompleted.size) *: millisecond
      } else {
        0 *: millisecond
      }

    Statistics(new DateTime(now), targetRps, startRateAverage, completeRateAverage, latencyAverage, -requestId, startTimes.size - requestsOrderedByCompletionTime.size)
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
      startTimes = startTimes + (myId -> System.currentTimeMillis())
      lg.makeNextRequest(myId) andThen {
        case _ => self ! RequestCompleted(myId, "TODO")
      }

      requestId -= 1
    }

    case RequestCompleted(requestId, content) => {
      val time = System.currentTimeMillis()
      requestsOrderedByCompletionTime = (requestId -> time) :: requestsOrderedByCompletionTime
      val ms = time - startTimes(requestId)
      latencies = latencies + (requestId -> ms)
      log.debug(s"request $requestId completed after $ms")
    }

    case GetStatistics(windowSize) => sender ! stats(windowSize)

    case PrintStatistics(windowSize) => log.info(stats(windowSize).toString)

  }

}
