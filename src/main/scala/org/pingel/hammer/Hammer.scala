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

  def setRpsIn(rps: Double, after: FiniteDuration) = {
    system.scheduler.schedule(after, 0.seconds, hammerActor, TargetRPS(rps))
  }

  def logStats(period: FiniteDuration) = {
    system.scheduler.schedule(
      0.millis,
      period,
      hammerActor,
      PrintStatistics())
  }

  lazy val vis = new Visualization(hammerActor)

  def connectionRatePlot() = vis.connectionRatePlot

}