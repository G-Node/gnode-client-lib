package org.gnode.util

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
