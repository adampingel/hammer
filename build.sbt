name := "hammer"

version := "0.1-SNAPSHOT"

organization := "org.pingel"

scalaVersion := "2.10.0"

// initialCommands in console := "import collection._"

resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2-M2",
  "com.typesafe.akka" %% "akka-agent" % "2.2-M2",
  "org.pingel" %% "axle" % "0.1-SNAPSHOT"
)