import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.kafkaflight",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3"
)


lazy val core = (project in file("core")).settings(
  commonSettings,
  libraryDependencies ++= backendDeps
)
