package org.gnode.lib.api

import dispatch._
import org.gnode.lib.conf._

object CallGenerator {
  def apply(config: Configuration): CallGenerator = new DefaultAPI(config)
}

trait CallGenerator {

  def authenticateUser(): Option[Request]
  def authenticateUser(username: String, password: String): Option[Request]

  def createObject(objectType: String): Option[Request]
  def updateObject(id: String): Option[Request]

  def getObject(id: String): Option[Request]
  def getData(id: String, options: Map[String, String] = Map()): Option[Request]

  def getParents(id: String): Option[Request]
  def getChildren(id: String): Option[Request]

  def getList(id: String, limit: Int = 0): Option[Request]
  def assign(id: String, options: Map[String, String] = Map()): Option[Request]

}

trait APIHelper {

  protected def pack(condition: Boolean = false, configuration: Configuration)(request: Request): Option[Request] =
    if (configuration.isIncomplete || condition) None
    else Some(request)

  // Nasty hack -- TODO: Discuss
  def split(id: String) = {
    try {
      val pos = id.indexOf("_")
      (id.substring(0, pos), id.substring(pos + 1))
    } catch {
      case _ => ("", "")
    }
  }

}

class DefaultAPI(config: Configuration) extends CallGenerator with APIHelper {

  private val configuration = config
  
  private val short_basis = :/(configuration.host) / configuration.prefix
  private val basis = short_basis / configuration.prefixData

  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  def authenticateUser(username: String, password: String): Option[Request] =
    pack(false, configuration) {
      val post_body = "username=" + username + "&password=" + password
      short_basis / "account" / "authenticate" / "" << post_body
    }

  def createObject(objectType: String): Option[Request] =
    pack(objectType.isEmpty, configuration) { (basis / objectType).PUT }

  def updateObject(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / split(id)._1 / split(id)._2 / "").POST
    }

  def getObject(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      basis / split(id)._1 / split(id)._2 / ""
    }

  def getData(id: String, options: Map[String, String] = Map()): Option[Request] =
    pack(id.isEmpty, configuration) {
      basis / "data" / id / "" <<? options
    }

  def getParents(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      basis / "parents" / id / ""
    }

  def getChildren(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      basis / "children" / id / ""
    }

  def getList(objectType: String, limit: Int = 0): Option[Request] =
    pack(objectType.isEmpty, configuration) {
      basis / objectType / "" <<? Map("range_start" -> limit.toString)
    }

  def assign(id: String, options: Map[String, String]): Option[Request] =
    pack(id.isEmpty, configuration) {
      basis / "assign" / id / "" <<? options
    }
  
}