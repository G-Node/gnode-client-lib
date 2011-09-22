package org.gnode.lib.client

// Internal packages
import org.gnode.lib.util._
import org.gnode.lib.conf._
import org.gnode.lib.neo._
import org.gnode.lib.api._
import org.gnode.lib.parse._
import org.gnode.lib.validate._

// External packages
import dispatch._

// Make log messages available globally
import LogMessages._

class Uploader(private val config: Configuration, private val http: Http) extends BatchTransfer {

  import org.gnode.lib.parse.ExtractError
  lazy val caller = CallGenerator(config)
  lazy val validator = new Validator(config)

  private def pushNew(no: NEObject, objectType: String): Option[String] = {

    import dispatch.liftjson.Js._
    import net.liftweb.json.JsonAST._

    val request = (caller.createObject(objectType) match {
      case Some(r) => r
      case None => throw new IllegalArgumentException
    }) <<< Writer.serialize(no).getOrElse("")

    val handler = request ># { json =>
      (json \ "neo_id") match {
	case JString(id) => Some(id)
	case _ => None
      }}

    try {
      http(handler)
    } catch {
      case StatusCode(400, _) =>
	logger error UPLOAD_NEW_BAD_REQUEST(objectType)
	None
    }

  }
    
  private def pushExisting(id: String, no: NEObject): Option[String] = {

    import dispatch.liftjson.Js._
    import net.liftweb.json.JsonAST._

    val request = (caller.updateObject(id) match {
      case Some(r) => r
      case None => throw new IllegalArgumentException
    }) <<< Writer.serialize(no).getOrElse("")

    val handler = request ># { json =>
      (json \ "neo_id") match {
	case JString(id) => Some(id)
	case _ => None
      }}
    
    try {
      http(handler)
    } catch {
      case StatusCode(400, _) =>
	logger error UPLOAD_UPDATE_BAD_REQUEST(id)
	None
    }

  }

  def add(id: String, obj: NEObject, objectType: Option[String] = None) {
    validator.validate(obj) match {
      case true =>
	jobs enqueue Job(id, Some(obj), objectType)
	logger info JOB_ADD(id)
      case false =>
	logger error UPLOAD_VALIDATION_ERROR
	logger error JOB_FAILURE("NEW_OBJECT")
    }
  }

  def push() = {

    import scala.collection.mutable.ListBuffer
    val b = ListBuffer[String]()

    while (!jobs.isEmpty) {

      val job = jobs.dequeue

      job.id match {
  	case "" =>
  	  pushNew(job.obj.get, job.objectType.get) match {
  	    case Some(id) => logger info JOB_COMPLETE(id); b += id
  	    case None => logger error JOB_FAILURE("NEW_OBJECT")
  	  }
  	case id: String =>
  	  pushExisting(id, job.obj.get) match {
  	    case Some(rid) => logger info JOB_COMPLETE(id); b += rid
  	    case None => logger error JOB_FAILURE(id)
  	  }
      }

    }

    Some(b.toList)

  }

}