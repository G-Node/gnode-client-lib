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

package org.gnode.lib.cache

import org.gnode.lib.neo.{NEOData, NEObject}
import org.gnode.lib.util._

class MemoryCache extends Cache with Loggable {

  case class CacheObject(obj: NEObject, etag: String)
  
  import scala.collection.mutable.Map
  private val cache = Map[String, CacheObject]()
  
  def add(obj: NEObject, etag: String = "") {

    val cObj = CacheObject(obj, etag)
    val id = obj.stringInfo.get("id")

    id match {
      case Some(curr_id) =>
	cache += (curr_id -> cObj)
      case _ =>
	throw new IllegalArgumentException
    }

  }

  def retrieve(id: String): Option[NEObject] =
    cache.get(id) match {
      case Some(CacheObject(obj, _)) => Some(obj)
      case None => None
    }

  def objectTag(id: String): Option[String] =
    cache.get(id) match {
      case Some(CacheObject(_, etag)) => Some(etag)
      case None => None
    }

  def delete(id: String) {
    cache -= id
  }

  def replace(id: String, obj: NEObject, etag: String) {
    delete(id)
    add(obj, etag)
  }
  
}
