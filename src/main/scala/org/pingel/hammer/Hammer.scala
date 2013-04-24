package org.pingel.hammer

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import HammerProtocol._

object Hammer {

  def main(args: Array[String]) {

    val requestsPerSecond = 2
    val duration = 20.seconds
    val lg = new ExampleLoadGenerator()

    val system = ActorSystem("HammerSystem")

    val hammerActor = system.actorOf(Props(new HammerActor(lg)))

    hammerActor ! TargetRPS(requestsPerSecond)

    system.scheduler.schedule(duration, 0.seconds, hammerActor, TargetRPS(0))

    system.scheduler.schedule(
      0.millis,
      5.seconds,
      hammerActor,
      PrintStatistics())

    val vis = new Visualization(hammerActor)
      
  }

}