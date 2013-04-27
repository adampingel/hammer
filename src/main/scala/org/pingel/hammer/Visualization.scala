package org.pingel.hammer

import akka.actor.{ Actor, ActorRef, ActorLogging, Cancellable }
import akka.pattern._
import akka.pattern.ask
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
// import collection._
import org.joda.time.DateTime
import axle.visualize._
import axle.visualize.Plottable._
import axle.quanta.Time._
import akka.util.Timeout
import HammerProtocol._

class Visualization(hammerActorRef: ActorRef) {

  implicit val askTimeout = Timeout(1.second)

  val d0 = new collection.immutable.TreeMap[DateTime, Double]()

  def connectionRatePlot() = {

    val initialData = List(
      ("open rate", d0), ("close rate", d0), ("target rate", d0)
    )

    val refreshFn = (previous: List[(String, collection.immutable.TreeMap[DateTime, Double])]) => {

      val statsFuture = (hammerActorRef ? HammerProtocol.GetStatistics()).mapTo[Statistics]
      val stats = Await.result(statsFuture, 40.milliseconds) // TODO await

      val t = new DateTime(stats.time)

      previous match {
        case open :: close :: target :: Nil => List(
          ("open rate", open._2 + (t -> stats.startRateAverage)),
          ("close rate", close._2 + (t -> stats.completeRateAverage)),
          ("target rate", target._2 + (t -> stats.targetRps))
        )
        case _ => Nil
      }
    }

    Plot(
      initialData,
      connect = true,
      title = Some("Hammer Stats"),
      xAxis = 0.0,
      xAxisLabel = Some("time (t)"),
      yAxis = new DateTime(),
      yAxisLabel = Some("connections / second"),
      refresher = Some(refreshFn, 1 *: second)
    )
  }

}