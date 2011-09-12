name := "gnode-client-lib"

version := "0.2"

organization := "org.gnode"

// Scala dependency
scalaVersion := "2.9.1"

// Twttr Maven Repo (util-*)
resolvers += "Twitter Maven Repository" at "http://maven.twttr.com"

// Release-time dependencies and libraries
libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.5",
  "net.databinder" %% "dispatch-lift-json" % "0.8.5",
  "net.databinder" %% "dispatch-json" % "0.8.5",
  "net.liftweb" %% "lift-json" % "2.4-M4",
  "com.twitter" % "util-logging" % "1.11.4"
)

// Development-time dependencies
libraryDependencies ++= Seq(
  "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test",
  "org.scala-tools.testing" % "scalatest" % "0.9.5" % "test"
)

seq(sbtassembly.Plugin.assemblySettings: _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")
