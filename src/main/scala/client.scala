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

trait HttpInteractor extends Loggable {
  
  // Create custom Http object that uses library-wide Twttr logging
  lazy val http = new Http with Loggable {
    override def make_logger = new Logger {

      def info(msg: String, items: Any*) { logger.info(msg, items: _*) }
      def warn(msg: String, items: Any*) { logger.warning(msg, items: _*) }
      
    }
  }

  def kill() {
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
  var authenticated = false
  
  def authenticate(): Boolean = {
    authenticate(config.username, config.password)
  }

  def authenticate(username: String, password: String): Boolean = {

    logger.info("Performing authentication for user " + username)
    
    val request = caller.authenticateUser(username, password) match {
      case Some(r) => r
      case _ => return false
    }

    try {

      val handler = request as_str

      val body = http(handler)
      authenticated = true
      return true

    } catch {

      case StatusCode(401, message) =>
	logger.error("Failure to authenticate for user " + username)
      case StatusCode(code, message) =>
	logger.error("General HTTP error " + code.toString + "(" + message + ")")
      case _ =>
	logger.error("General error while authenticating user " + username)
      
    }

    authenticated = false
    return false

  }

}

trait BatchTransfer extends Loggable {

  case class Job(id: String, obj: Option[NEObject] = None)

  import scala.collection.mutable.Queue
  val jobs = Queue[Job]()

}

class Downloader(private val config: Configuration, private val http: Http) extends BatchTransfer {

  import org.gnode.lib.parse.ExtractError

  lazy val caller = CallGenerator(config)
  lazy val cache = Cache(config.caching)

  def list(objectType: String, limit: Int = 0): Option[List[String]] = {

    logger.info("Retrieving list for type " + objectType)
    
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
	logger.error("Problem while parsing object list")
	return None
      case StatusCode(404, _) =>
	logger.error("Object type doesn't exist")
	return None
      case StatusCode(x, _) =>
	logger.error("Generic HTTP error (" + x.toString + ")")
	return None
      case _ =>
	logger.error("Generic error")
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
	  logger.error("Could not parse " + id)
	  return None
      }

    } catch {
      
      // Cacheable
      case StatusCode(304, _) =>
	logger.info("Cache hit for " + id + "@" + tag.getOrElse(""))
	return cache.retrieve(id)
      case e: java.net.NoRouteToHostException =>
	logger.info("Disconnected. Trying cache for " + id + "@" + tag.getOrElse(""))
	cache.retrieve(id) match {
	  case Some(o) => logger.info("Cache hit for " + id + "@" + tag.getOrElse("")); return Some(o)
	  case None => logger.info("Cache miss"); return None
	}

      // Non-cacheable
      case StatusCode(404, _) =>
	logger.error("Object " + id + " was not found")
	return None
      case StatusCode(401, _) =>
	logger.error("You are not authorised to request object " + id)
	return None
      case StatusCode(code, message) =>
	logger.error("Generic HTTP error " + code.toString + " (" + message + ")")
	return None
      case e =>
	logger.error("Unknown error while retrieving " + id + " (" + e + ")")
	return None

    }

  }
    
  def add(id: String) {
    jobs enqueue Job(id)
    logger info "Enqueued new download job: " + id
  }

  def get() = {

    import scala.collection.mutable.ListBuffer
    val b = ListBuffer[NEObject]()
    
    while (!jobs.isEmpty) {
      val job = jobs dequeue
      val obj = pull(job.id)

      obj match {
	case Some(o: NEObject) =>
	  logger info "Job successfully completed: " + job.id
	  b += o
	case _ => logger error "Job failure: " + job.id
      }

    }

    Some(b.toList)

  }

}

class Uploader(private val config: Configuration, private val http: Http) extends BatchTransfer
