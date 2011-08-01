name := "G-Node JVM Library"

version := "0.1"

scalaVersion := "2.9.0-1"

libraryDependencies += "junit" % "junit" % "4.8" % "test"

libraryDependencies += "org.yaml" % "snakeyaml" % "1.8"

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.4"
)

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-lift-json" % "0.8.4"
)

libraryDependencies += "net.liftweb" % "lift-json" % "2.0"
