package org.pingel.hammer

import scala.concurrent.duration.FiniteDuration

object HammerProtocol {

  case class Start(stopAfter: Option[FiniteDuration])

  case class Stop()

  case class StartNextRequest()

  case class RequestCompleted(requestId: Long, content: String)

  case class GetStatistics()

  case class Statistics(time: Long, targetRps: Double, startRateAverage: Double, completeRateAverage: Double, totalRequests: Long) {

    override def toString(): String = s"""
Hammer statistics

  Current time: $time
  Target RPS: $targetRps
  Average # requests started per second: $startRateAverage
  Average # requests completed per second: $completeRateAverage
  Total # requests: $totalRequests
    """

  }

  case class PrintStatistics()
  
}
