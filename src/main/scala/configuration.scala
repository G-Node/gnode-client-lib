package org.gnode.lib.conf

import org.gnode.lib.util.Loggable
import org.gnode.lib.util.File._

// Encapsulates configuration data. Emitted by ConfigurationReader; accepted by Connector.
case class Configuration(username: String,
			 password: String,
			 host: String,
			 port: Int,
			 path: String,
			 apiDefinition: String,
			 caching: String,
			 db: String) {

  def isIncomplete(): Boolean =
    host.isEmpty || port == 0

}
    
object ConfigurationReader extends Loggable {
  
  /** Parameterized configuration generator. */
  def create(username: String, 
	     password: String,
	     host: String,
	     port: Int,
	     path: String,
	     apiDefinition: String,
	     caching: String,
	     db: String) = Configuration(username, password, host, port, path, apiDefinition, caching, db)

  /** Wrapper around fromString for straightforward configuration loading from file */
  def fromFile(filename: String): Option[Configuration] = {

    try {

      using(io.Source.fromFile(filename)) { file =>
	fromString(file.mkString)
      }

    } catch {

      case e: java.io.FileNotFoundException => {
	logger.error("Configuration file (" + filename + ") not found")
	None
      }
      case _ => {
	logger.error("Error while importing from file (" + filename + ")")
	None
      }

    }

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