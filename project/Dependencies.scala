import sbt._

object Dependencies {
  // Versions
  lazy val akkaVersion = "2.4.18"

  // Libraries
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.8"
  val akkaSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.8"

  val akkaStreamsKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "0.17"

  val akkaQuartz = "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.0-akka-2.4.x"

  val scalactic = "org.scalactic" %% "scalactic" % "3.0.1"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

  val cassandraCore = "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0"
  val cassandraMapping ="com.datastax.cassandra" % "cassandra-driver-mapping" % "3.0.0"

  // Projects
  val backendDeps =
    Seq(akkaActor,
      akkaStreamsKafka,
      akkaHttp,
      akkaSprayJson,
      akkaQuartz,
      cassandraCore,
      cassandraMapping,
      scalaTest % Test,
      scalactic % Test)
}
