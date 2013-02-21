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
import org.gnode.lib.cache._
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

trait HttpInteractor extends Loggable {
  
  // Create custom Http object that uses library-wide Twttr logging
  lazy val http = new Http with HttpsLeniency with Loggable {
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

  val validator = new Validator(config)

  // Delegator
  private val d = new Downloader(config, http)
  private val u = new Uploader(config, http, validator)
  private val a = new Authenticator(config, http)

  // Put "authenticated" in scope
  import a._

  def clearCache {
    d.cache.clear
  }

  // HDF5 download utility
  def downloadData(location: String) =
    Network.downloadFile(http, location)

  // HDF5 upload utility
  def uploadData(file_location: String): String =
    authenticated {
      val req = u.caller.createDatafile.get
      Network.uploadFile(http, file_location, req, "raw_file")
    }

  // Sharing
  def shareObject(id: String, safety_level: Int, users: Array[String], levels: Array[Int], cascade: Boolean) =
    authenticated {
      u shareObject(id, safety_level, users, levels, cascade)
    }

  // Retrieval methods

  def addDown(id: String) =
    d add id

  def retrieve =
    authenticated {
      (d get).get.toArray
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

  def retrieve(ids: Array[String]) =
    authenticated {
      ids foreach d.add
      (d get).get.toArray
    }

  // List

  def retrieveList(objectType: String, limit: Int, startIndex: Int, searchTerms: Array[String]): Array[String] =
    authenticated {
      d.list(objectType, limit, startIndex, searchTerms).getOrElse(List()).toArray
    }

  def retrieveList(objectType: String, limit: Int, startIndex: Int): Array[String] =
    authenticated {
      d.list(objectType, limit, startIndex, Array()).getOrElse(List()).toArray
    }

  // Generic push command

  def push =
    authenticated {
      u push
    }

  // Create

  def addCreate(obj: NEObject, objectType: String) =
    u add ("", obj, Some(objectType))

  def addCreate(objects: Array[NEObject], objectType: String) =
    objects foreach { u add ("", _, Some(objectType)) }

  def addCreate(objects: Array[(NEObject, String)]) =
    objects foreach { pair => u add ("", pair._1, Some(pair._2)) }

  def create(obj: NEObject, objectType: String) =
    authenticated {
      u add ("", obj, Some(objectType))
      u push
    }

  def create(objects: Array[NEObject], objectType: String) =
    authenticated {
      objects foreach { u add ("", _, Some(objectType)) }
      u push
    }

  def create(objects: Array[(NEObject, String)]) =
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

  def addUpdate(objects: Array[NEObject]) =
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

  def update(objects: Array[NEObject]) =
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
