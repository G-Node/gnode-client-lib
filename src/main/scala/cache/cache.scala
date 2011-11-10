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

sealed abstract class CacheType

object CacheType {
  
  case object MONGO extends CacheType
  case object SQLITE extends CacheType
  case object MEMORY extends CacheType

  def apply(cacheType: String): CacheType = cacheType match {
 
    case "MONGO" => MONGO
    case "SQLITE" => SQLITE
    case "MEMORY" => MEMORY

    case _ =>
      throw new IllegalArgumentException

  }

}

/* Cache factory. Produces an implementation of Cache that can be used
   by the HTTP processor. */
object Cache {
  
  import CacheType._
  
  def apply(c: CacheType): Cache = c match {

    case MEMORY => new MemoryCache
    case _ => new MemoryCache

  }

  def apply(c: String): Cache = apply(CacheType(c))

}

abstract class Cache {

  // Constructive
  def add(obj: NEObject, etag: String = "")
  def retrieve(id: String): Option[NEObject]

  // Destructive
  def delete(id: String)
  def replace(id: String, obj: NEObject, etag: String)

  // Information
  def objectTag(id: String): Option[String]

}