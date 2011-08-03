package org.gnode.lib

import org.gnode.lib.util.Loggable
import org.gnode.lib.conf.{Configuration, ConfigurationReader}
import org.gnode.lib.neo._

import dispatch._
import dispatch.liftjson.Js._

import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonParser

import scala.reflect.Manifest

/** Primary mediator of interaction with the G-Node Client Library. Instance corresponds to session. '''Crucial''':
 * Needs to be kill()'ed in order to avoid memory leaks, given that automated resource management (AutoClose)
 * is currently restricted to the buggy Java 7 VM.*/

class Connector(configuration: Configuration) extends Loggable {

  // Enable logging
  logInit(this.toString)
  
  /** HTTP client */
  lazy val http = new Http
  
  /** Local configuration */
  private val config: Configuration = configuration

  /** Auxiliary constructor. Returns `Connection` with default configuration. */
  def this() = this(ConfigurationReader.default)
  /** Auxiliary constructor. Returns `Connection` with configuration from specified JSON resource. */
  def this(filename: String) = this(ConfigurationReader.fromFile(filename).getOrElse(null))

  /** In absence of Java 7's AutoClose: a kill-switch. */
  def kill = {
    this.http.shutdown()
  }

  /** Generates URL basis from Configuration. */
  private def urlBase(c: Configuration): String =
    "http://" + c.host + ":" + c.port + c.path

  /** Authenticates session based on credentials specified in `Configuration`. */
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
    if (!success) {  // Why, oh why no ternary operator, Scala.
      logger.error("Log-in failure. Check credentials")
    } else {
      logger.info("Successful log-in")
    }

  }

  /** Primary and generic retrieval system. */
  private def pull[T: Manifest](req: Request): Option[T] = {

    implicit val formats = DefaultFormats

    try {

      Some(http(req ># { json =>
	json.extract[T] }))

    } catch {

      case StatusCode(400, _) => logger.error("Request failed"); None
      case e => logger.error("Unknown error: " + e); None

    }

  }

  /** Generic. Pulls object with corresponding ID. */
  def getByID[T: Manifest](id: String): Option[T] = {
    
    val query = url(urlBase(this.config)) / "neo" / id
    pull[T](query)

  }

  /** Pulls list of available objects with `objectType: String`. */
  def getList(objectType: String): Option[NEObjectList] = {

    val query = url(urlBase(this.config)) / "neo" / "select" / objectType
    pull[NEObjectList](query)

  }

  def getBlock(id: String): NEOBlock = getByID[NEOBlock](id).getOrElse(null)
  def getSignal(id: String): NEOAnalogSignal = getByID[NEOAnalogSignal](id).getOrElse(null)
  def getSegment(id: String): NEOSegment = getByID[NEOSegment](id).getOrElse(null)
  def getEvent(id: String): NEOEvent = getByID[NEOEvent](id).getOrElse(null)
  def getEpoch(id: String): NEOEpoch = getByID[NEOEpoch](id).getOrElse(null)
  def getUnit(id: String): NEOUnit = getByID[NEOUnit](id).getOrElse(null)
  def getSpikeTrain(id: String): NEOSpikeTrain = getByID[NEOSpikeTrain](id).getOrElse(null)
  def getSpike(id: String): NEOSpike = getByID[NEOSpike](id).getOrElse(null)
  def getRecordingChannel(id: String): NEORecordingChannel = getByID[NEORecordingChannel](id).getOrElse(null)
  
}
