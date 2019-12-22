name := "ScalaProjects"

version := "0.1"

scalaVersion := "2.12.10"

val pulsar4sVersion = "2.4.4"

libraryDependencies ++= Seq(
  "com.sksamuel.pulsar4s" %% "pulsar4s-core" % pulsar4sVersion,

  // for the akka-streams integration
  "com.sksamuel.pulsar4s" %% "pulsar4s-akka-streams" % pulsar4sVersion,

  "com.typesafe.scala-logging"      %% "scala-logging"     % "3.9.2",

  "ch.qos.logback"                  %  "logback-classic"   % "1.2.3",

  // if you want to use circe for schemas
  "com.sksamuel.pulsar4s" %% "pulsar4s-circe" % pulsar4sVersion,

  "org.apache.kafka"                %  "kafka-streams"                            % "2.3.0",
  "com.typesafe.akka"               %% "akka-stream-kafka"                        % "1.0.5"


)

