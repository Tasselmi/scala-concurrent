name := "scala-concurrent"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.4",
    "org.scala-lang.modules" %% "scala-async" % "0.9.1",
    "com.github.scala-blitz" %% "scala-blitz" % "1.2",
    "com.netflix.rxjava" % "rxjava-scala" % "0.19.1",
    "org.scala-lang.modules" %% "scala-swing" % "1.0.1",
    "org.scala-stm" %% "scala-stm" % "0.7",
    "com.typesafe.akka" %% "akka-actor" % "2.3.2",
    "com.typesafe.akka" %% "akka-remote" % "2.3.2",
    "com.storm-enroute" %% "scalameter-core" % "0.6",
    "org.scalaz" %% "scalaz-concurrent" % "7.0.6",
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.4",
    "com.quantifind" %% "wisp" % "0.0.4",
    "org.scalafx" %% "scalafx" % "1.0.0-R8",
    "com.storm-enroute" %% "reactive-collections" % "0.5"
)

//fork := true