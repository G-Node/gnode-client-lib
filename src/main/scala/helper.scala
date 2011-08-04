package org.gnode.lib.util

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
