package org.gnode.lib.util

import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import java.util.{Map => JMap}

import org.gnode.lib.neo._

/** This trait provides basic reflection functionality for all NEObjects, as well as a
 * convenient prettyPrint method for CL usage of org.gnode.lib functions. */

trait NEOReflector extends Product {

  /** Reflects attributes of extended class. Primary use case is printing of
   * parsed NEObjects, as well as rebuilding during remote storage of NEObjects. */

  def getFields: List[(String, Any)] = {
    var fieldValueToName: Map[Any, String] = Map()
    for (field <- getClass.getDeclaredFields) {
      field.setAccessible(true)
      fieldValueToName += (field.get(this) -> field.getName)
    }
    productIterator.toList map { value => fieldValueToName(value) -> value }
  }

  def unpack: JMap[String, Any] = {

    var myMap: Map[String, Any] = Map()

    for (field <- getClass.getDeclaredFields) {
      field.setAccessible(true)
      myMap += field.getName -> (field.get(this) match {
	case l: List[String] => l.toArray
	case n: NEObject => n.unpack
	case e => e
      })
    }

    myMap

  }
      

  /** Human-readable printing of parsed NEObject. */

  def prettyPrint = {

    val fields = this.getFields
    fields.foreach { t =>
      println((" " * (25 - t._1.size)) + t._1 + " => " + t._2.toString) }

  }

}

/** Utility object. Provides file management support (e.g., auto-closing of
 * resources). */

object File {

  /** Helper function. Offers auto-close functionality for close()-providing
   * resources. */

  def using[T <: { def close() }]
  (resource: T)
  (block: T => Unit) {
    
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }

  }

}
