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
import axle.algebra.Plottable._
import axle.quanta.Time._
import akka.util.Timeout
import HammerProtocol._

class Visualization(hammerActorRef: ActorRef) {

  implicit val askTimeout = Timeout(1.second)

  val d0 = new collection.immutable.TreeMap[DateTime, Double]()

  def connectionRatePlot(windowSize: Long, viewWidth: Long) = {

    val initialData = List(
      ("open rate", d0), ("close rate", d0), ("target rate", d0)
    )

    val refreshFn = (previous: List[(String, collection.immutable.TreeMap[DateTime, Double])]) => {

      val now = System.currentTimeMillis()
      val viewCutoff = now - viewWidth

      val statsFuture = (hammerActorRef ? HammerProtocol.GetStatistics(windowSize)).mapTo[Statistics]
      val stats = Await.result(statsFuture, 40.milliseconds) // TODO await

      val t = new DateTime(stats.time)

      previous match {
        case open :: close :: target :: Nil => List(
          ("open rate", d0 ++ (open._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.startRateAverage)),
          ("close rate", d0 ++ (close._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.completeRateAverage)),
          ("target rate", d0 ++ (target._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.targetRps))
        )
        case _ => Nil
      }
    }

    Plot(
      initialData,
      connect = true,
      title = Some("Connection Rates"),
      xAxis = Some(0.0),
      xAxisLabel = Some("time (t)"),
      yAxisLabel = Some("connections / second"),
      refresher = Some(refreshFn, 1 *: second)
    )
  }

  def latencyPlot(windowSize: Long, viewWidth: Long) = {

    val initialData = List(("latency", d0))

    val refreshFn = (previous: List[(String, collection.immutable.TreeMap[DateTime, Double])]) => {

      val now = System.currentTimeMillis()
      val viewCutoff = now - viewWidth

      val statsFuture = (hammerActorRef ? HammerProtocol.GetStatistics(windowSize)).mapTo[Statistics]
      val stats = Await.result(statsFuture, 40.milliseconds) // TODO await

      val t = new DateTime(stats.time)

      val next = previous match {
        case latency :: Nil => List(
          ("latency", (d0 ++ (latency._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.latencyAverage)))
        )
        case _ => Nil
      }

      next
    }

    Plot(
      initialData,
      connect = true,
      title = Some("Response Latency"),
      xAxis = Some(0.0),
      xAxisLabel = Some("time (t)"),
      //yAxis = Some(new DateTime()),
      yAxisLabel = Some("milliseconds"),
      refresher = Some(refreshFn, 1 *: second)
    )
  }

}