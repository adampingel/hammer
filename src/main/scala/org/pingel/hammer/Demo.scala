package org.pingel.hammer

object Demo {

  def main(args: Array[String]) {

    import dispatch._ // , Defaults._
    import scala.concurrent.ExecutionContext.Implicits.global
    import util.Random.nextInt

    class HostIpApiLoadGenerator extends LoadGenerator {

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

    val hammer = new Hammer(new HostIpApiLoadGenerator(), 2d)
    
    import scala.concurrent.duration._
    hammer.logStats(5.seconds)

    // hammer.rps(0.1d)

    import axle.visualize._
    show(hammer.connectionRatePlot)
    show(hammer.latencyPlot)

  }

}