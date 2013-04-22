package org.pingel.hammer

import scala.concurrent.duration.DurationInt

import HammerProtocol.Start
import HammerProtocol.Stop
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

object Hammer {

  def main(args: Array[String]) {

    import HammerProtocol._

    val rps = 0.5
    val duration = 10.seconds
    val lg = new ExampleLoadGenerator()

    val system = ActorSystem("HammerSystem")
    
    val hammerActor = system.actorOf(Props(new HammerActor(lg, rps)))

    hammerActor ! Start(Some(duration))

  }

}