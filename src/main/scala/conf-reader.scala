package org.gnode.lib.conf

import org.gnode.lib.util.Loggable
import org.gnode.lib.util.{File => f}

// Encapsulates configuration data. Emitted by ConfigurationReader; accepted by Connector.
case class Configuration(username: String,
			 password: String,
			 host: String,
			 port: Int,
			 path: String,
			 apiVersion: String)

object ConfigurationReader extends Loggable {

  // See: org.gnode.lib.util.Loggable
  logInit(getClass.toString)

  /** Sane default configuration, hard-coded. */
  val default: Configuration = create("bob", "pass", "hal10.g-node.pri")

  /** Parameterized configuration generator. */
  def create(username: String, 
	     password: String,
	     host: String,
	     port: Int = 80,
	     path: String = "/",
	     apiVersion: String = "v1") = Configuration(username, password, host, port, path, apiVersion)

  /** Wrapper around fromString for straightforward configuration loading from file */
  def fromFile(filename: String): Option[Configuration] = {

    var config: Option[Configuration] = None

    try {

      f.using(io.Source.fromFile(filename)) { file =>
	config = fromString(file.mkString)
      }

    } catch {

      case e: java.io.FileNotFoundException => {
	logger.error("Configuration file (" + filename + ") not found")
      }
      case _ => {
	logger.error("Error while importing from file (" + filename + ")")
      }

    }

    config

  }

  /** Creates configuration object from JSON string. */
  def fromString(config: String): Option[Configuration] = {

    import net.liftweb.json._
    implicit val formats = DefaultFormats

    try {

      val json = parse(config)
      return Some(json.extract[Configuration])

    } catch {

      case e => {
	logger.error("Parsing error: " + e)
	None
      }

    }

  }
    
}
