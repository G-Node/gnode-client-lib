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

  // private def pushNew(obj: NEObject): Option[String] = {
   
  // }
    
  // private def pushExisting(id: String, obj: NEObject): Boolean

  // def add(id: String, obj: NEObject) {
  //   jobs enqueue Job(id, Some(obj))
  //   logger info JOB_ADD(id)
  // }

  // def push() = {

  //   import scala.collection.mutable.ListBuffer
  //   val b = ListBuffer[String]()

  //   while (!jobs.isEmpty) {

  //     val job = jobs.dequeue

  //     job.id match {
  // 	case "" =>
  // 	  pushNew(job.obj.get) match {
  // 	    case Some(id) => logger info JOB_COMPLETE(id); b += id
  // 	    case None => logger error JOB_FAILURE("NEW_OBJECT")
  // 	  }
  // 	case id: String =>
  // 	  pushExisting(id, job.obj.get) match {
  // 	    case true => logger info JOB_COMPLETE(id)
  // 	    case false => logger error JOB_FAILURE(id)
  // 	  }
  //     }

  //   }

  //   Some(b.toList)

  // }

}