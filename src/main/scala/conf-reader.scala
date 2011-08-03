package org.gnode.lib.conf

import org.gnode.lib.util.Loggable
import org.gnode.lib.util.{File => f}

// Encapsulates configuration data. Emitted by ConfigurationReader; accepted by Connector.
case class Configuration(val username: String,
		    val password: String,
		    val host: String,
		    val port: Int,
		    val path: String,
		    val apiVersion: String)

object ConfigurationReader extends Loggable {

  // See: org.gnode.lib.util.Loggable
  logInit(getClass.toString)

  // Parameterized version
  def create(username: String, 
	     password: String,
	     host: String,
	     port: Int = 80,
	     path: String = "/",
	     apiVersion: String = "v1") = new Configuration(username, password, host, port, path, apiVersion)

  // Wrapper around fromString for straightforward configuration loading from file
  def fromFile(filename: String): Configuration = {

    var config: Configuration = null

    try {

      f.using(io.Source.fromFile(filename)) { file =>
	config = fromString(file.mkString)
      }

    } catch {

      case e: java.io.FileNotFoundException => {
	logger.error("Configuration file (" + filename + ") not found")
	config = null
      }
      case _ => {
	logger.error("Error while importing from file (" + filename + ")")
	config = null
      }

    }

    return config

  }

  // Creates configuration object from JSON string
  def fromString(config: String): Configuration = {

    import net.liftweb.json._
    implicit val formats = DefaultFormats

    try {

      val json = parse(config)
      return json.extract[Configuration]

    } catch {

      case e => {
	logger.error("Parsing error: " + e)
	null
      }

    }

  }
    
}
