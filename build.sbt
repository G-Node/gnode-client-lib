name := "G-Node JVM Library"

version := "0.1"

// Scala dependency
scalaVersion := "2.8.1"

// JUnit Testing Framework/Harnesses/Fixtures
// Possibly ScalaTest?
libraryDependencies += "junit" % "junit" % "4.8" % "test"

// YAML Parser
libraryDependencies += "org.yaml" % "snakeyaml" % "1.8"

// HTTP Library
libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.4"
)

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-lift-json" % "0.8.4"
)

// JSON Parsing and Emitting
libraryDependencies += "net.liftweb" % "lift-json" % "2.0"

seq(sbtassembly.Plugin.assemblySettings: _*)
