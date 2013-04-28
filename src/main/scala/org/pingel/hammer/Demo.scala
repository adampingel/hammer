package org.pingel.hammer

import scala.concurrent.duration._

object Demo {

  def main(args: Array[String]) {

    val lg = new LoadGenerator {

      import dispatch._ // , Defaults._
      import scala.concurrent.ExecutionContext.Implicits.global
      import util.Random.nextInt

      val requestBuilders = Vector(
        url("http://api.hostip.info/get_json.php"),
        url("http://api.hostip.info/country.php")
      //url("http://www.cnn.com/")
      )

      def randomRequestBuilder() = requestBuilders(nextInt(requestBuilders.size))

      def makeNextRequest(id: Long) = {
        Http(randomRequestBuilder() OK as.String)
      }
    }

    val hammer = new Hammer(lg, 2d)
    hammer.logStats(5.seconds)

    // hammer.rps(0.1d)

    import axle.visualize._
    show(hammer.connectionRatePlot)
    show(hammer.latencyPlot)

  }

}