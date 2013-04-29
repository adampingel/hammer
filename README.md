hammer
======

Also see Demo.scala.

The Hammer class requires a LoadGenerator.

The following load generator uses
[Dispatch](http://dispatch.databinder.net/Dispatch.html) library
to make asynchronous HTTP requests.

```scala

import org.pingel.hammer._
import dispatch._ // , Defaults._
import scala.concurrent.ExecutionContext.Implicits.global
import util.Random.nextInt

class HostIpApiLoadGenerator extends LoadGenerator {

  def name() = "hostip.info API"

  val requestBuilders = Vector(
    url("http://api.hostip.info/get_json.php"),
    url("http://api.hostip.info/country.php")
  )

  def randomRequestBuilder() = requestBuilders(nextInt(requestBuilders.size))

  def makeNextRequest(id: Long) = {
    Http(randomRequestBuilder() OK as.String)
  }
}
```

Now start the load generator, issuing 2 reqeusts per second (2 Hz):

```scala
import axle.quanta._
import Frequency._

val hammer = new Hammer(new HostIpApiLoadGenerator(), 2 *: Hz)
```

Log connection open/closed rates every 5 seconds with:

```scala
import Time._
hammer.logStatsEvery(5 *: second)
```

Example output:

```
[...]
[INFO] [04/22/2013 01:49:15.615] [HammerSystem-akka.actor.default-dispatcher-3] [akka://HammerSystem/user/$a] 
Hammer statistics

  Current time: 1366620555612
  Target RPS: 1.0
  Average # requests started per second: 1.0470160043874956
  Average # requests completed per second: 0.9971580994166626
  Total # requests: 21
```

To repeat this output, clone this repository and do `sbt run`.

Set the target requests/second after the hammer is running:

```scala
hammer.rps(0.2 *: Hz)
```

Create a plots for
# target rate as well as the connections opened and closed rate
# latency average

```scala
import axle.visualize._
import axle.algebra.Plottable._

implicit val hzP = Hz.plottable
show(hammer.connectionRatePlot())

implicit val msP = ms.plottable
show(hammer.latencyPlot())
```

![hammervis](./doc/image/hammer.png)

