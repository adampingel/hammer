package org.pingel.hammer

import scala.concurrent.duration.FiniteDuration
import axle.quanta._

object HammerProtocol {

  // case class Start(stopAfter: Option[FiniteDuration])
  // case class Stop()

  case class TargetRPS(target: Frequency.Q)

  case class StartNextRequest()

  case class RequestCompleted(requestId: Long, content: String)

  case class GetStatistics(windowSize: Long)

  case class Statistics(
    time: Long,
    targetRps: Frequency.Q,
    startRateAverage: Frequency.Q,
    completeRateAverage: Frequency.Q,
    latencyAverage: Time.Q,
    totalRequests: Long,
    pendingRequests: Long) {

    override def toString(): String = s"""
Hammer statistics

  Current time: $time
  Target RPS: $targetRps
  Average # requests started per second: $startRateAverage
  Average # requests completed per second: $completeRateAverage
  Latency average: $latencyAverage milliseconds
  Current # pending requests: $pendingRequests
  Total # requests: $totalRequests
    """

  }

  case class PrintStatistics(windowSize: Long)

}
