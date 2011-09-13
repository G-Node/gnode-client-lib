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

  private lazy val short_basis = :/(configuration.host)
  private lazy val basis = :/(configuration.host) / configuration.path

  private def pack(condition: Boolean = false)(request: Request): Option[Request] =
    if (this.configuration.isIncomplete) None
    else if (condition) throw new IllegalArgumentException
    else Some(request)

  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  def authenticateUser(username: String, password: String): Option[Request] =
    pack(false) {
      val post_body = "username=" + username + "&password=" + password
      short_basis / "account" / "authenticate" / "" << post_body
    }

  def createObject(): Option[Request] =
    pack(false) { (basis / "").PUT }

  def updateObject(id: String): Option[Request] =
    pack(id.isEmpty) {
      (basis / id / "").POST
    }

  def getObject(id: String): Option[Request] =
    pack(id.isEmpty) {
      basis / id / ""
    }

  def getData(id: String, options: Map[String, String] = Map()): Option[Request] =
    pack(id.isEmpty) {
      basis / "data" / id / "" <<? options
    }

  // def getData(id: String,
  // 	      startTime: Long,
  // 	      endTime: Long,
  // 	      duration: Long,
  // 	      startIndex: Long,
  // 	      endIndex: Long,
  // 	      samplesCount: Long,
  // 	      downsample: Long) = {}

}
