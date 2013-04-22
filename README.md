hammer
======

Hammer.scala defines a main that demonstrates this code's usage:

```scala
val rps = 1 // one request per second
val duration = 5.seconds
val lg = new ExampleLoadGenerator()

val system = ActorSystem("HammerSystem")
    
val hammerActor = system.actorOf(Props(new HammerActor(lg, rps)))

hammerActor ! HammerProtocol.Start(Some(duration))
```

See the ExampleLoadGenerator.scala, which uses the
[Dispatch](http://dispatch.databinder.net/Dispatch.html) library to make asynchronous
HTTP requests.
