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

  def retrieveSingle(id: String) =
    authenticated {
      d.add(id)
      d get match {
	case Some(o :: tail) => Some(o)
	case Some(List()) => None
	case _ => None
      }
    }

  def retrieveMany(ids: List[String]) =
    authenticated {
      ids foreach d.add
      d get
    }

  def retrieveList(objectType: String, limit: Int = 0) =
    authenticated {
      d.list(objectType, limit)
    }

  def createSingle(o: NEObject, objectType: String) =
    authenticated {
      u add ("", o, Some(objectType))
      u push
    }

  def createMany(o: List[NEObject], objectType: String) =
    authenticated {
      o foreach { u add ("", _, Some(objectType)) }
      u push
    }

  def createMany(o: List[(NEObject, String)]) =
    authenticated {
      o foreach { pair => u add ("", pair._1, Some(pair._2)) }
      u push
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