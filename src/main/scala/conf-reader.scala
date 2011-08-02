package org.gnode.conf

import net.liftweb.json._

import org.gnode.util.Loggable
import org.gnode.util.{File => f}

// Encapsulates configuration data. Emitted by ConfigurationReader; accepted by Connector.
class Configuration(val username: String,
		    val password: String,
		    val host: String,
		    val port: Int,
		    val path: String,
		    val apiVersion: String) extends Loggable

object ConfigurationReader extends Loggable{

  // See: org.gnode.util.Loggable
  logInit()

  // Parameterized version
  def create(username: String, password: String, host: String, port: Int = 80, path: String = "/", apiVersion: String = "v1") =
    new Configuration(username, password, host, port, path, apiVersion)

  // Wrapper around fromString for straightforward configuration loading from file
  def fromFile(filename: String): Configuration = {

    var config: Configuration = null

    try {

      f.using(io.Source.fromFile(filename)) { file =>
	config = fromString(file.mkString)
      }

    } catch {

      case e: java.io.FileNotFoundException => logger.error("Configuration file (" + filename + ") not found."); config = null
      case _ => logger.error("Error while importing from file (" + filename + ")"); config = null

    }

    return config

  }

  // Creates configuration object from JSON string
  def fromString(config: String): Configuration = {

    new Configuration("", "", "", 0, "", "")

  }
    

}
