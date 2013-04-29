package org.pingel.hammer

import akka.actor.{ Actor, ActorRef, ActorLogging, Cancellable }
import akka.pattern._
import akka.pattern.ask
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
import org.joda.time.DateTime
import axle.visualize._
import axle.algebra.Plottable._
import axle.quanta._
import akka.util.Timeout
import HammerProtocol._
import Time._
import Frequency._

class Visualization(hammerActorRef: ActorRef, loadGeneratorName: String) {

  implicit val askTimeout = Timeout(1.second)

  val d0f = new collection.immutable.TreeMap[DateTime, Frequency.Q]()
  val d0t = new collection.immutable.TreeMap[DateTime, Time.Q]()

  def connectionRatePlot(windowSize: Long, viewWidth: Long) = {

    val initialData = List(
      ("open rate", d0f), ("close rate", d0f), ("target rate", d0f)
    )

    val refreshFn = (previous: List[(String, collection.immutable.TreeMap[DateTime, Frequency.Q])]) => {

      val now = System.currentTimeMillis()
      val viewCutoff = now - viewWidth

      val statsFuture = (hammerActorRef ? HammerProtocol.GetStatistics(windowSize)).mapTo[Statistics]
      val stats = Await.result(statsFuture, 40.milliseconds) // TODO await

      val t = new DateTime(stats.time)

      previous match {
        case open :: close :: target :: Nil => List(
          ("open rate", d0f ++ (open._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.startRateAverage)),
          ("close rate", d0f ++ (close._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.completeRateAverage)),
          ("target rate", d0f ++ (target._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.targetRps))
        )
        case _ => Nil
      }
    }

    implicit val hzp = Hz.plottable

    Plot(
      initialData,
      connect = true,
      pointDiameter = 0,
      title = Some(s"$loadGeneratorName Connection Rates"),
      xAxis = Some(0 *: Hz),
      xAxisLabel = Some("time"),
      yAxisLabel = Some("connections / second"),
      refresher = Some(refreshFn, 1 *: second)
    )
  }

  def latencyPlot(windowSize: Long, viewWidth: Long) = {

    val initialData = List(("latency", d0t))

    val refreshFn = (previous: List[(String, collection.immutable.TreeMap[DateTime, Time.Q])]) => {

      val now = System.currentTimeMillis()
      val viewCutoff = now - viewWidth

      val statsFuture = (hammerActorRef ? HammerProtocol.GetStatistics(windowSize)).mapTo[Statistics]
      val stats = Await.result(statsFuture, 40.milliseconds) // TODO await

      val t = new DateTime(stats.time)

      previous match {
        case latency :: Nil => List(
          ("latency", (d0t ++ (latency._2 filterKeys { _.isAfter(viewCutoff) }) + (t -> stats.latencyAverage)))
        )
        case _ => Nil
      }
    }

    implicit val msp = ms.plottable

    Plot(
      initialData,
      connect = true,
      pointDiameter = 0,
      title = Some(s"$loadGeneratorName Response Latency"),
      xAxis = Some(0 *: millisecond),
      xAxisLabel = Some("time"),
      yAxisLabel = Some("milliseconds"),
      refresher = Some(refreshFn, 1 *: second)
    )
  }

}