package org.pingel.hammer

import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
import collection._
import akka.actor._
import akka.pattern._
import akka.pattern.ask
import org.joda.time.DateTime
import axle.visualize._
import axle.visualize.Plottable._
import axle.quanta.Time._
import akka.util.Timeout
import HammerProtocol._

class Visualization(hammerActor: ActorRef) {

  implicit val askTimeout = Timeout(1.second)

  val initialData = List(
    ("open rate", new immutable.TreeMap[DateTime, Double]()),
    ("close rate", new immutable.TreeMap[DateTime, Double]()),
    ("target rate", new immutable.TreeMap[DateTime, Double]())
  )

  val refreshFn = (previous: List[(String, immutable.TreeMap[DateTime, Double])]) => {

    val statsFuture = (hammerActor ? HammerProtocol.GetStatistics()).mapTo[Statistics]
    val stats = Await.result(statsFuture, 40.milliseconds)

    val t = new DateTime(stats.time)
    import stats._

    previous match {
      case open :: close :: target :: Nil => List(
        ("open rate", open._2 + (t -> startRateAverage)),
        ("close rate", close._2 + (t -> completeRateAverage)),
        ("target rate", target._2 + (t -> targetRps))
      )
      case _ => Nil
    }
  }

  val openClosePlot = Plot(
    initialData,
    connect = true,
    title = Some("Hammer Stats"),
    xAxis = 0.0,
    xAxisLabel = Some("time (t)"),
    yAxis = new DateTime(),
    yAxisLabel = Some("connections / second"),
    refresher = Some(refreshFn, 1 *: second)
  )

  show(openClosePlot)
  
}