package org.pingel.hammer

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import axle.visualize._
import axle.quanta._
import HammerProtocol._

class Hammer(loadGenerator: LoadGenerator, initialRPS: Frequency.Q) {

  val system = ActorSystem("HammerSystem")

  val hammerActor = system.actorOf(Props(new HammerActor(loadGenerator)))

  hammerActor ! TargetRPS(initialRPS)

  def rps(rps: Frequency.Q) = {
    // system.scheduler.schedule(after, 0.seconds, hammerActor, TargetRPS(rps))
    hammerActor ! TargetRPS(rps)
  }

  import Time._
  
  def logStatsEvery(period: Time.Q, windowSize: Long = 10000L) = {
    val periodFD = (period in millisecond).magnitude.toDouble.millis
    system.scheduler.schedule(0.millis, periodFD, hammerActor, PrintStatistics(windowSize))
  }

  lazy val vis = new Visualization(hammerActor, loadGenerator.name)

  def connectionRatePlot(windowSize: Long = 10000L, viewWidth: Long = 120000L) = vis.connectionRatePlot(windowSize, viewWidth)

  def latencyPlot(windowSize: Long = 10000L, viewWidth: Long = 120000L) = vis.latencyPlot(windowSize, viewWidth)

}