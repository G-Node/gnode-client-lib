package org.gnode.lib.client

// Internal packages
import org.gnode.lib.cache._
import org.gnode.lib.util._
import org.gnode.lib.conf._
import org.gnode.lib.neo._
import org.gnode.lib.api._
import org.gnode.lib.parse._

// External packages
import dispatch._

// Make log messages available globally
import LogMessages._

trait HttpInteractor extends Loggable {
  
  // Create custom Http object that uses library-wide Twttr logging
  lazy val http = new Http with Loggable {
    override def make_logger = new Logger {

      def info(msg: String, items: Any*) { /* logger.info(msg, items: _*) */ }
      def warn(msg: String, items: Any*) { logger.warning(msg, items: _*) }
      
    }
  }

  def kill() {
    logger info HTTP_SHUTDOWN
    http.shutdown()
  }

}

class TransferManager(private val config: Configuration) extends HttpInteractor {

  // Delegator
  private val d = new Downloader(config, http)
  private val u = new Uploader(config, http)
  private val a = new Authenticator(config, http)

  // Put "authenticated" in scope
  import a._

  // Retrieval methods

  def retrieve =
    authenticated {
      d get
    }

  def retrieve(id: String) =
    authenticated {
      d.add(id)
      d get match {
	case Some(o :: tail) => Some(o)
	case Some(List()) => None
	case _ => None
      }
    }

  def retrieve(ids: List[String]) =
    authenticated {
      ids foreach d.add
      d get
    }

  def retrieveList(objectType: String, limit: Int = 0) =
    authenticated {
      d.list(objectType, limit)
    }

  // Generic push command

  def push =
    authenticated {
      u push
    }

  // Create

  def addCreate(obj: NEObject, objectType: String) =
    u add ("", obj, Some(objectType))

  def addCreate(objects: List[NEObject], objectType: String) =
    objects foreach { u add ("", _, Some(objectType)) }

  def addCreate(objects: List[(NEObject, String)]) =
    objects foreach { pair => u add ("", pair._1, Some(pair._2)) }

  def create(obj: NEObject, objectType: String) =
    authenticated {
      u add ("", obj, Some(objectType))
      u push
    }

  def create(objects: List[NEObject], objectType: String) =
    authenticated {
      objects foreach { u add ("", _, Some(objectType)) }
      u push
    }

  def create(objects: List[(NEObject, String)]) =
    authenticated {
      objects foreach { pair => u add ("", pair._1, Some(pair._2)) }
      u push
    }

  // Update

  def addUpdate(id: String, obj: NEObject) =
    u add (id, obj)

  def addUpdate(obj: NEObject) =
    u add (guessIdentifier(obj), obj)

  def addUpdate(objects: Map[String, NEObject]) =
    for ((id, obj) <- objects) u add (id, obj)

  def addUpdate(objects: List[NEObject]) =
    objects foreach { obj => u add (guessIdentifier(obj), obj) }

  def update(id: String, obj: NEObject) =
    authenticated {
      u add (id, obj)
      u push
    }

  def update(obj: NEObject) =
    authenticated {
      u add (guessIdentifier(obj), obj)
      u push
    }

  def update(objects: Map[String, NEObject]) =
    authenticated {
      for ((id, obj) <- objects) u add (id, obj)
      u push
    }

  def update(objects: List[NEObject]) =
    authenticated {
      objects foreach { obj => u add (guessIdentifier(obj), obj) }
      u push
    }

  /* Connect
   TODO */

  // Queue operations

  def clearUp {
    u clear
  }

  def clearDown {
    d clear
  }

  // Util

  private def guessIdentifier(obj: NEObject) =
    obj.stringInfo.isDefinedAt("neo_id") match {
      case true => obj.stringInfo("neo_id")
      case false => throw new IllegalArgumentException
    }
  
}

trait BatchTransfer extends Loggable {

  case class Job(id: String, obj: Option[NEObject] = None, objectType: Option[String] = None)

  import scala.collection.mutable.Queue
  val jobs = Queue[Job]()

  def clear {
    jobs clear
  }

}