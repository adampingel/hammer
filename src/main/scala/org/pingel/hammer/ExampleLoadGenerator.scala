package org.pingel.hammer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import util.Random.nextInt

class ExampleLoadGenerator extends LoadGenerator {

  import dispatch._ // , Defaults._

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
