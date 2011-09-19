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