package org.gnode.lib.cache

import org.gnode.lib.neo.{NEOData, NEObject}
import org.gnode.lib.util._

class MemoryCache extends Cache with Loggable {

  case class CacheObject(obj: NEObject, etag: String)
  
  import scala.collection.mutable.Map
  private val cache = Map[String, CacheObject]()
  
  def add(obj: NEObject, etag: String = "") {

    val cObj = CacheObject(obj, etag)
    val neo_id = obj.stringInfo.get("neo_id")

    neo_id match {
      case Some(id) =>
	cache += (id -> cObj)
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