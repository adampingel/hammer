hammer
======

See the ExampleLoadGenerator.scala, which uses the
[Dispatch](http://dispatch.databinder.net/Dispatch.html) library to make asynchronous
HTTP requests.

Hammer.scala defines a main that demonstrates this code's usage:

```scala
val rps = 1 // one request per second
val duration = 20.seconds
val lg = new ExampleLoadGenerator()

val system = ActorSystem("HammerSystem")

val hammerActor = system.actorOf(Props(new HammerActor(lg, rps)))

hammerActor ! HammerProtocol.Start(Some(duration))

system.scheduler.schedule(0.millis, 5.seconds, hammerActor, HammerProtocol.PrintStatistics())
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
