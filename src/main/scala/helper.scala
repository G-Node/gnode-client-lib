package org.gnode.lib.util

trait NEOReflector extends Product {

  def getFields: List[(String, Any)] = {
    var fieldValueToName: Map[Any, String] = Map()
    for (field <- getClass.getDeclaredFields) {
      field.setAccessible(true)
      fieldValueToName += (field.get(this) -> field.getName)
    }
    productIterator.toList map { value => fieldValueToName(value) -> value }
  }

  def prettyPrint = {

    val fields = this.getFields
    fields.foreach { t =>
      println((" " * (20 - t._1.size)) + t._1 + " => " + t._2.toString) }

  }

}

object File {

  // Helper function. Automatically closes resources after using them.
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
