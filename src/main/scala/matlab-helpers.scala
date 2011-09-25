package org.gnode.lib.matlab

object Helper {

  def isNone[T](that: Option[T]) =
    that match {
      case None => true
      case _ => false
    }

}
