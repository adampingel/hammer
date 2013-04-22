package org.pingel.hammer

import akka.actor.{ Props, Actor, ActorRef, ActorSystem, ActorLogging }
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Cancellable
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait LoadGenerator {
  def makeNextRequest(): Future[String]
}

class HammerActor(lg: LoadGenerator, targetRps: Double) extends Actor {

  import HammerProtocol._

  val averageOver = 5d // seconds over which to compute average rps

  var requestSchedule: Option[Cancellable] = None

  val timestamps = collection.mutable.Set[Long]()

  def receive = {

    case Start() => {
      requestSchedule = Some(context.system.scheduler.schedule(
        0.millis,
        (1 / targetRps).seconds,
        self,
        StartNextRequest()))
    }

    case StartNextRequest() => {
      lg.makeNextRequest() map { println(_) }
    }

    case Stop() => {
      requestSchedule.map(_.cancel)
      requestSchedule = None
      context.system.shutdown
    }

    case RPS() => sender ! (timestamps.size / 5d)
  }

}

object HammerProtocol {

  case class Start()
  case class Stop()
  case class StartNextRequest()
  case class RPS()
}

object Hammer {

  val lg = new LoadGenerator {

    // import dispatch._, Defaults._
    // val cnn = url("http://api.hostip.info/get_json.php")
    // val country = url("http://api.hostip.info/country.php")

    def makeNextRequest() = concurrent.Future { "Hello" }
    // Http((if (util.Random.nextBoolean) cnn else country) OK as.String)

  }

  def main(args: Array[String]) {

    import HammerProtocol._

    val rps = 0.5
    val duration = 10.seconds
    
    val system = ActorSystem("HammerSystem")
    val hammerActor = system.actorOf(Props(new HammerActor(lg, rps)))

    hammerActor ! Start()

    system.scheduler.schedule(duration, 0.seconds, hammerActor, Stop())

  }

}