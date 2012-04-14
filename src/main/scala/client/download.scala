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

// External packages
import dispatch._

// Make log messages available globally
import LogMessages._

class Downloader(private val config: Configuration, private val http: Http) extends BatchTransfer {

  import org.gnode.lib.parse.ExtractError

  lazy val caller = CallGenerator(config)
  lazy val cache = Cache(config.caching)

  def list(objectType: String, limit: Int, startIndex: Int): Option[List[String]] = {

    logger info RETRIEVE_LIST_START(objectType)
    
    val request = caller.getList(objectType, limit, startIndex) match {
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
