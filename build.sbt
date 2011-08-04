name := "gnode-client-lib"

version := "0.1"

organization := "org.gnode"

// Scala dependency
scalaVersion := "2.9.0-1"

// Twttr Maven Repo (util-*)
resolvers += "Twitter Maven Repository" at "http://maven.twttr.com"

// util-logging
libraryDependencies += "com.twitter" % "util-logging" % "1.10.4"

// HTTP Library
libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.4"
)

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-lift-json" % "0.8.4"
)

// JSON Parsing and Emitting
libraryDependencies += "net.liftweb" % "lift-json_2.9.0-1" % "2.4-M3"

seq(sbtassembly.Plugin.assemblySettings: _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")
