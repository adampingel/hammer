package org.pingel.hammer

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._

object Hammer {

  def main(args: Array[String]) {

    val rps = 1 // one request per second
    val duration = 20.seconds
    val lg = new ExampleLoadGenerator()

    val system = ActorSystem("HammerSystem")

    val hammerActor = system.actorOf(Props(new HammerActor(lg, rps)))

    hammerActor ! HammerProtocol.Start(Some(duration))

    system.scheduler.schedule(
      0.millis,
      5.seconds,
      hammerActor,
      HammerProtocol.PrintStatistics())

  }

}