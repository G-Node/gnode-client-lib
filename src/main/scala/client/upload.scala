/* Copyright (C) 2011 by German Neuroinformatics Node
 * www.g-node.org

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */

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

class Uploader(private val config: Configuration, private val http: Http, private val validator: Validator) extends BatchTransfer {

  import org.gnode.lib.parse.ExtractError
  import org.gnode.lib.util.IDExtractor._

  lazy val caller = CallGenerator(config)

  private def pushNew(no: NEObject, objectType: String): Option[String] = {

    import dispatch.liftjson.Js._
    import net.liftweb.json.JsonAST._

    val request = (caller.createObject(objectType) match {
      case Some(r) => r
      case None => throw new IllegalArgumentException
    }) << Writer.serialize(no).getOrElse("")

    logger info request.method
    logger info request.path

    val handler = request ># { json =>
      (json \ "permalink") match {
	case JString(permalink) => Some(extractID(permalink))
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
    }) << Writer.serialize(no).getOrElse("")

    logger info request.method
    logger info request.path

    val handler = request ># { json =>
      (json \ "permalink") match {
	case JString(permalink) => Some(extractID(permalink))
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
    validator.validate(obj, objectType.getOrElse("")) match {
      case true =>
	jobs enqueue Job(id, Some(obj), objectType)
	logger info JOB_ADD(if (id.isEmpty) "NEW_OBJECT" else id)
      case false =>
	logger error UPLOAD_VALIDATION_ERROR
	logger error JOB_FAILURE("NEW_OBJECT")
    }
  }

  def push() = {

    import scala.collection.mutable.ArrayBuffer
    val b = ArrayBuffer[String]()

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

    b.toArray

  }

}
