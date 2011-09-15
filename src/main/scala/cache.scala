package org.gnode.lib.cache

import org.gnode.lib.neo.{NEOData, NEObject}

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

  def apply(c: CacheType): Cache = c match {

    case MONGO => new MongoCache
    case SQLITE => new SqliteCache
    case MEMORY => new MemoryCache
    
    case _ => new MemoryCache

  }

}

abstract class Cache {

  /* Caching

  Suggested flow:
  * Retrieving system checks if cache holds object with given ID
    -> If not, retrieve/fail.
  * If yes, retrieve E-Tag.
  * Request with E-Tag. Check status code:
    -> 200 + E-Tag
	-> Store object in cache
	-> Return
    -> 304
	-> Retrieve object from cache
	-> Return
  * If HTTP failure: Either return object from cache, or nothing!

  */
  
  
  // Constructive
  def add(obj: NEObject, etag: String)
  def retrieve(id: String): Option[NEObject]

  // Destructive
  def delete(id: String)
  def replace(id: String, obj: NEObject, etag: String)

  // Information
  def objectTag(id: String): Option[String]

}

class MemoryCache extends Cache with Loggable {

  case class CacheObject(obj: NEObject, etag: String)
  
  import scala.collection.mutable.Map
  private val cache = new Map[String, CacheObject]
  
  //def add(obj: NEObject, etag: String): Option[String]

}
