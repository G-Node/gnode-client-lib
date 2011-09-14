package org.gnode.lib.util

/** Utility object. Provides file management support (e.g., auto-closing of
 * resources). */

object File {

  /** Helper function. Offers auto-close functionality for close()-providing
   * resources. */

  def using[T <: { def close() }, R](resource: T)(block: T => R): R = {
    
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }

  }

}


