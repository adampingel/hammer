hammer
======

See the ExampleLoadGenerator.scala, which uses the
[Dispatch](http://dispatch.databinder.net/Dispatch.html) library to make asynchronous
HTTP requests.

An example usage (also see Demo.scala):

```scala
import axle.visualize._
import scala.concurrent.duration._

val lg = new LoadGenerator {

  import dispatch._ // , Defaults._
  import scala.concurrent.ExecutionContext.Implicits.global
  import util.Random.nextInt

  val requestBuilders = Vector(
    url("http://api.hostip.info/get_json.php"),
    url("http://api.hostip.info/country.php")
  )

  def randomRequestBuilder() = requestBuilders(nextInt(requestBuilders.size))

  def makeNextRequest(id: Long) = {
    Http(randomRequestBuilder() OK as.String)
  }
}

val hammer = new Hammer(lg, 2d)
hammer.logStats(5.seconds)
hammer.setRpsIn(0d, 20.seconds)
    
show(hammer.connectionRatePlot)
```

Example output:

```
[...]
[INFO] [04/22/2013 01:49:10.705] [HammerSystem-akka.actor.default-dispatcher-3] [akka://HammerSystem/user/$a] request 15 completed after 93 milliseconds
[INFO] [04/22/2013 01:49:11.711] [HammerSystem-akka.actor.default-dispatcher-3] [akka://HammerSystem/user/$a] request 16 completed after 100 milliseconds
[INFO] [04/22/2013 01:49:12.717] [HammerSystem-akka.actor.default-dispatcher-2] [akka://HammerSystem/user/$a] request 17 completed after 106 milliseconds
[INFO] [04/22/2013 01:49:14.151] [HammerSystem-akka.actor.default-dispatcher-3] [akka://HammerSystem/user/$a] request 18 completed after 540 milliseconds
[INFO] [04/22/2013 01:49:14.713] [HammerSystem-akka.actor.default-dispatcher-2] [akka://HammerSystem/user/$a] request 19 completed after 102 milliseconds
[INFO] [04/22/2013 01:49:15.615] [HammerSystem-akka.actor.default-dispatcher-3] [akka://HammerSystem/user/$a] 
Hammer statistics

  Current time: 1366620555612
  Target RPS: 1.0
  Average # requests started per second: 1.0470160043874956
  Average # requests completed per second: 0.9971580994166626
  Total # requests: 21
```

To repeat this output, clone this repository and do `sbt run`.
