package org.pingel.hammer

import scala.concurrent.duration.FiniteDuration
import axle.quanta._
import org.joda.time.DateTime

object HammerProtocol {

  // case class Start(stopAfter: Option[FiniteDuration])
  // case class Stop()

  case class TargetRPS(target: Frequency.Q)

  case class StartNextRequest()

  case class RequestCompleted(requestId: Long, content: String)

  case class GetStatistics(windowSize: Long)

  case class Statistics(
    time: DateTime,
    targetRps: Frequency.Q,
    startRateAverage: Frequency.Q,
    completeRateAverage: Frequency.Q,
    latencyAverage: Time.Q,
    totalRequests: Long,
    pendingRequests: Long) {

    override def toString(): String = s"""
Hammer statistics

  Current time: $time
  Target request start rate: $targetRps
  Average request start rate: $startRateAverage
  Average request completion rate: $completeRateAverage
  Latency average: $latencyAverage
  Current # pending requests: $pendingRequests
  Total # requests: $totalRequests
    """

  }

  case class PrintStatistics(windowSize: Long)

}
