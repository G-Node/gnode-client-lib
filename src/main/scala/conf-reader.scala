package org.gnode.conf

import org.gnode.log._
import net.liftweb.json._
import org.gnode.util.{File => f}

// Encapsulates configuration data. Emitted by ConfigurationReader; accepted by Connector.
class Configuration(host: String, port: Int, path: String, apiVersion: String) extends Loggable

object ConfigurationReader extends Loggable{

  logInit()

  // Parameterized version
  def create(host: String, port: Int, path: String, apiVersion: String) =
    new Configuration(host, port, path, apiVersion)

  // Wrapper around fromString for straightforward configuration loading from file
  def fromFile(filename: String): Configuration = {

    var ret: Configuration = null

    f.using(io.Source.fromFile(filename)) { file =>
      ret = fromString(file.mkString)
    }

    return ret

  }

  // Creates configuration object from JSON string
  def fromString(config: String): Configuration =
    

}
