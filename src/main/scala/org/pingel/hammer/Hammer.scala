package org.pingel.hammer

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import axle.visualize._
import HammerProtocol._

class Hammer(loadGenerator: LoadGenerator, initialRequestsPerSecond: Double) {

  val system = ActorSystem("HammerSystem")

  val hammerActor = system.actorOf(Props(new HammerActor(loadGenerator)))

  hammerActor ! TargetRPS(initialRequestsPerSecond)

  def rps(rps: Double) = {
    // system.scheduler.schedule(after, 0.seconds, hammerActor, TargetRPS(rps))
    hammerActor ! TargetRPS(rps)
  }

  def logStats(period: FiniteDuration, windowSize: Long = 10000L) = {
    system.scheduler.schedule(0.millis, period, hammerActor, PrintStatistics(windowSize))
  }

  lazy val vis = new Visualization(hammerActor, loadGenerator.name)

  def connectionRatePlot(windowSize: Long = 10000L, viewWidth: Long = 120000L) = vis.connectionRatePlot(windowSize, viewWidth)

  def latencyPlot(windowSize: Long = 10000L, viewWidth: Long = 120000L) = vis.latencyPlot(windowSize, viewWidth)

}