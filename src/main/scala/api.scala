package org.gnode.lib.api

import dispatch._
import org.gnode.lib.conf._

trait CallGenerator {

  def authenticateUser(): Option[Request]
  def authenticateUser(username: String, password: String): Option[Request]

  def createObject(): Option[Request]
  def updateObject(id: String): Option[Request]

  def getObject(id: String): Option[Request]

}

class DefaultCallGenerator(val configuration: Configuration) extends CallGenerator {

  lazy val short_basis = :/(configuration.host)
  lazy val basis = :/(configuration.host) / configuration.path

  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  def authenticateUser(username: String, password: String): Option[Request] =
    if (configuration.isIncomplete) None else {
      val post_body = "username=" + username + "&password=" + password
      Some(short_basis / "account" / "authenticate" / "" << post_body)
    }

  def createObject(): Option[Request] =
    if (configuration.isIncomplete) None else Some((basis / "").PUT)

  def updateObject(id: String): Option[Request] =
    if (configuration.isIncomplete) None
    else if (id.isEmpty) throw new IllegalArgumentException
    else Some((basis / id / "").POST)

  def getObject(id: String): Option[Request] =
    if (configuration.isIncomplete) None
    else if (id.isEmpty) throw new IllegalArgumentException
    else Some(basis / id / "")

  def getData(id: String): Option[Request] =
    if (configuration.isIncomplete) None
    else if (id.isEmpty) throw new IllegalArgumentException
    else Some(basis / "data" / id / "")
    
}
