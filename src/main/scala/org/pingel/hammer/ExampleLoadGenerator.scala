package org.pingel.hammer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ExampleLoadGenerator extends LoadGenerator {

  import dispatch._ // , Defaults._
  
  val cnn = url("http://api.hostip.info/get_json.php")
  
  val country = url("http://api.hostip.info/country.php")

  def randomRequestBuilder() = if (util.Random.nextBoolean) cnn else country
  
  def makeNextRequest() = Http(randomRequestBuilder() OK as.String)

}
