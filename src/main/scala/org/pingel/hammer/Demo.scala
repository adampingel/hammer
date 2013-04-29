package org.pingel.hammer

object Demo {

  def main(args: Array[String]) {

    import dispatch._ // , Defaults._
    import scala.concurrent.ExecutionContext.Implicits.global
    import util.Random.nextInt

    class HostIpApiLoadGenerator extends LoadGenerator {

      def name() = "hostip.info API"
      
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

    import axle.quanta._
    import Frequency._

    val hammer = new Hammer(new HostIpApiLoadGenerator(), 2 *: Hz)
    
    import Time._
    hammer.logStatsEvery(5 *: second)

    // hammer.rps(0.1d)

    import axle.visualize._
    import axle.algebra.Plottable._
    implicit val msp = ms.plottable

    //show(hammer.connectionRatePlot())
    show(hammer.latencyPlot())

  }

}