package org.gnode.lib.matlab

import org.gnode.lib.api._

object Helper {

  def isNone[T](that: Option[T]) =
    that match {
      case None => true
      case _ => false
    }

  def guessType(id: String) = {
    val splitter = new APIHelper {}
    val parts = splitter.split(id)

    if (parts._1.isEmpty) throw new Exception("Could not approximate object type")
    else parts._1
  }

}
