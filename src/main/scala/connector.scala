package org.gnode.lib

import org.gnode.lib.util.Loggable
import org.gnode.lib.conf.{Configuration, ConfigurationReader}
import org.gnode.lib.neo.{NEObject, NEOBlock, NEObjectList}

import dispatch._
import dispatch.liftjson.Js._

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

  /** In absence of Java 7's AutoClose: a kill-switch. */
  def kill = {
    this.http.shutdown()
  }

  /** Generates URL basis from Configuration. */
  private def urlBase(c: Configuration): String =
    "http://" + c.host + ":" + c.port + c.path

  /** Authenticates the ongoing G-Node session based on credentials specified in configuration. */
  def authenticate: Unit = {

    val query = url(urlBase(this.config)) / "account" / "login/"

    // Subsequent authentication test is exceedingly brittle; at this point,
    // however, no solid confirmation technique available due to
    //   a) an invalid XHTML response (non-parseable by scala.xml._) as well as
    //   b) lack of appropriate 401 response.
    // Hack will be fixed once server response unequivocal.

    val success: Boolean = http(query << Map("username" -> config.username, "password" -> config.password) >- { text =>
      text.contains("What Next?") })

    // TODO: Throw appropriate exception
    if (!success) {
      logger.error("Log-in failure. Check credentials")
    } else {
      logger.info("Successful log-in")
    }

  }
  
  /** Generic. Pulls object with corresponding ID. */
  def getByID[T: Manifest](id: String): Option[T] = {

    implicit val formats = DefaultFormats
    
    val query = url(urlBase(this.config)) / "neo" / id

    try {

      Some(http(query ># { json =>
	json.extract[T] }))

    } catch {

      case StatusCode(400, _) => logger.error("ID not found"); None
      case _ => logger.error("Unknown error"); None

    }

  }

  /** Pulls list of available objects with `objectType: String`. */
  def getList(objectType: String): Option[NEObjectList] = {

    implicit val formats = DefaultFormats

    val query = url(urlBase(this.config)) / "neo" / "select" / objectType

    try {

      Some(http(query ># { json =>
	json.extract[NEObjectList] }))

    } catch {

      case StatusCode(400, _) => logger.error("Object type not found"); None
      case _ => logger.error("Unknown error"); None

    }

  }
  
}
