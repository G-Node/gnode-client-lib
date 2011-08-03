package org.gnode.lib

import org.gnode.lib.util.Loggable
import org.gnode.lib.conf.{Configuration, ConfigurationReader}
import org.gnode.lib.neo.{NEObject, NEOBlock, NEObjectList}

import dispatch._
import net.liftweb.json._
import net.liftweb.json.JsonAST._

import scala.reflect.Manifest

/** Primary mediator of interaction with the G-Node Client Library. Encapsulates a "data session". */
class Connector(configuration: Configuration) extends Loggable {

  // Enable logging
  logInit(this.toString)
  
  // Default Dispatch HTTP object
  val http = new Http() 
  
  // Local configuration
  val config: Configuration = configuration

  /** Auxiliary constructor. Returns `Connection` with default configuration. */
  def this() = this(ConfigurationReader.default)
  /** Auxiliary constructor. Returns `Connection` with configuration from specified JSON resource. */
  def this(filename: String) = this(ConfigurationReader.fromFile(filename))

  /** Authenticates the ongoing G-Node session based on credentials specified in configuration. */
  def authenticate: Unit = {

    val query = url("http://" + config.host + ":" + config.port) / config.path / "account" / "login/"
    http(query << Map("username" -> config.username, "password" -> config.password) >|)

  }

  /** Generic. Pulls object with corresponding ID. */
  def getByID[T: Manifest](id: String): T = {

    implicit val formats = DefaultFormats
    
    val query = url("http://" + config.host + ":" + config.port) / config.path / "neo" / id
    val jsonResult = (http(query as_str)).toString
    (parse(jsonResult)).extract[T]

  }
  
}
