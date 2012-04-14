/* Copyright (C) 2011 by German Neuroinformatics Node
 * www.g-node.org

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */

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

  def getList(id: String, limit: Int = 0, startIndex: Int = 0): Option[Request]
  def assign(id: String, options: Map[String, String] = Map()): Option[Request]

}

trait APIHelper {

  protected def pack(condition: Boolean = false, configuration: Configuration)(request: Request): Option[Request] =
    if (configuration.isIncomplete || condition) None
    else Some(request)

  // Hack -- TODO: Discuss
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
  
  private val short_basis = :/(configuration.host, configuration.port) / configuration.prefix
  private val basis = short_basis / configuration.prefixData

  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  def authenticateUser(username: String, password: String): Option[Request] =
    pack(false, configuration) {
      val post_body = "username=" + username + "&password=" + password
      (short_basis / "account" / "authenticate" / "" << post_body)
    }

  def createObject(objectType: String): Option[Request] =
    pack(objectType.isEmpty, configuration) { (basis / objectType).PUT }

  def updateObject(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / split(id)._1 / split(id)._2 / "").POST
    }

  def getObject(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / split(id)._1 / split(id)._2 / "")
    }

  def getData(id: String, options: Map[String, String] = Map()): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / "data" / id / "" <<? options)
    }

  def getParents(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / "parents" / id / "")
    }

  def getChildren(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / "children" / id / "")
    }

  def getList(objectType: String, limit: Int, startIndex: Int): Option[Request] =
    pack(objectType.isEmpty, configuration) {
      (basis / objectType / "" <<? Map("max_results" -> limit.toString,
				       "offset" -> startIndex.toString))
    }

  def assign(id: String, options: Map[String, String]): Option[Request] =
    pack(id.isEmpty, configuration) {
      (basis / "assign" / id / "" <<? options)
    }
  
}
