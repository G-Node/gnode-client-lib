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
  
}

class Authenticator(private val config: Configuration, private val http: Http) extends Loggable {

  lazy val caller = CallGenerator(config)
  var auth = false

  def authenticated[T](block: => T): T =
    if (!this.auth) {
      this.authenticate()
      block
    } else {
      block
    }
  
  def authenticate(): Boolean = {
    authenticate(config.username, config.password)
  }

  def authenticate(username: String, password: String): Boolean = {

    logger info AUTHENTICATE_BEGIN(username)
    
    val request = caller.authenticateUser(username, password) match {
      case Some(r) => r
      case _ => return false
    }

    try {

      val handler = request as_str

      val body = http(handler)
      auth = true
      return true

    } catch {

      case StatusCode(401, message) =>
	logger error AUTHENTICATE_FAILURE(username)
      case StatusCode(code, message) =>
	logger error HTTP_GENERAL(code, message)
      	logger error AUTHENTICATE_ERROR(username)
      case _ =>
	logger error AUTHENTICATE_ERROR(username)
      
    }

    auth = false
    return false

  }

}

trait BatchTransfer extends Loggable {

  case class Job(id: String, obj: Option[NEObject] = None)

  import scala.collection.mutable.Queue
  val jobs = Queue[Job]()

  def clear {
    jobs clear
  }

}

class Downloader(private val config: Configuration, private val http: Http) extends BatchTransfer {

  import org.gnode.lib.parse.ExtractError

  lazy val caller = CallGenerator(config)
  lazy val cache = Cache(config.caching)

  def list(objectType: String, limit: Int = 0): Option[List[String]] = {

    logger info RETRIEVE_LIST_START(objectType)
    
    val request = caller.getList(objectType, limit) match {
      case Some(r) => r
      case None => throw new IllegalArgumentException
    }

    val handler = request as_str

    try {

      val body = http(handler)
      return Reader.makeListOpt(body)
      
    } catch {

      case e: ExtractError =>
	logger error RETRIEVE_LIST_ERROR_PARSE(objectType)
	return None
      case StatusCode(404, _) =>
	logger error RETRIEVE_LIST_ERROR_404(objectType)
	return None
      case StatusCode(code, message) =>
	logger error HTTP_GENERAL(code, message)
	return None
      case _ =>
	logger error RETRIEVE_LIST_ERROR_GENERIC(objectType)
	return None

    }

  }

  private def pull(id: String): Option[NEObject] = {
    
    val tag = cache.objectTag(id)
    
    var headers = Map[String, String]()
    
    tag match {
      case Some(t) => headers = Map("If-None-Match" -> t)
      case _ =>
    }

    val request = caller.getObject(id) match {
      case Some(r) => r <:< headers
      case None => throw new IllegalArgumentException
    }

    val handler = request >+ { req =>
      (req as_str, req >:> { _("ETag") }) }

    try {

      val (body, info) = http(handler)

      // 200
      val obj = Reader.makeObjectOpt(body)
      obj match {
	case Some(o) =>
	  cache.replace(id, o, info.head)
	  return obj
	case None =>
	  logger error PARSE_ERROR(id)
	  return None
      }

    } catch {
      
      // Cacheable
      case StatusCode(304, _) =>
	logger info CACHE_HIT(id, tag.getOrElse(""))
	return cache.retrieve(id)
      case e: java.net.NoRouteToHostException =>
	logger info CACHE_TRY(id, tag.getOrElse(""))
	cache.retrieve(id) match {
	  case Some(o) => logger info CACHE_HIT(id, tag.getOrElse("")); return Some(o)
	  case None => logger info CACHE_MISS(id, tag.getOrElse("")); return None
	}

      // Non-cacheable
      case StatusCode(404, _) =>
	logger error RETRIEVE_OBJECT_ERROR_404(id)
	return None
      case StatusCode(401, _) =>
	logger error RETRIEVE_OBJECT_ERROR_NOT_AUTHORISED(id)
	return None
      case StatusCode(code, message) =>
	logger error HTTP_GENERAL(code, message)
	return None
      case e =>
	logger error RETRIEVE_OBJECT_ERROR_GENERIC(id)
	return None

    }

  }
    
  def add(id: String) {
    jobs enqueue Job(id)
    logger info JOB_ADD(id)
  }

  def get() = {

    import scala.collection.mutable.ListBuffer
    val b = ListBuffer[NEObject]()
    
    while (!jobs.isEmpty) {
      val job = jobs dequeue
      val obj = pull(job.id)

      obj match {
	case Some(o: NEObject) =>
	  logger info JOB_COMPLETE(job.id)
	  b += o
	case _ => logger error JOB_FAILURE(job.id)
      }

    }

    Some(b.toList)

  }

}

class Uploader(private val config: Configuration, private val http: Http) extends BatchTransfer