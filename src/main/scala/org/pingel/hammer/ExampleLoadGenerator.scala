package org.pingel.hammer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ExampleLoadGenerator extends LoadGenerator {

  import dispatch._ // , Defaults._

  val requestBuilders = Vector(
    url("http://api.hostip.info/get_json.php"),
    url("http://api.hostip.info/country.php"),
    url("http://www.cnn.com/")
  )

  def randomRequestBuilder() = requestBuilders(util.Random.nextInt(requestBuilders.size))

  def makeNextRequest() = Http(randomRequestBuilder() OK as.String)

}
