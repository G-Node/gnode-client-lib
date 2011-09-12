package org.gnode.lib.api

import dispatch._
import org.gnode.lib.conf._

trait CallGenerator {

  def authenticateUser(): Option[Request]
  def authenticateUser(username: String, password: String): Option[Request]

  def createObject(): Option[Request]
  def updateObject(id: String): Option[Request]

}

class DefaultCallGenerator(val configuration: Configuration) extends CallGenerator {

  lazy val basis = :/(configuration.host) / configuration.path

  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  def authenticateUser(username: String, password: String): Option[Request] =
    if (configuration.isIncomplete) None else {
      val post_body = "username=" + username + "&password=" + password
      Some(basis / "account" / "authenticate" / "" << post_body)
    }

  def createObject(): Option[Request] =
    if (configuration.isIncomplete) None else Some((basis / "neo" / "").PUT)

  def updateObject(id: String = ""): Option[Request] =
    if (configuration.isIncomplete) None else Some((basis / "neo" / id / "").POST)
    
}
