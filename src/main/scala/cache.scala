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

// class MongoCache extends
