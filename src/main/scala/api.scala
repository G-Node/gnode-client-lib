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
  def getObject(id: String, full: Boolean = true): Option[Request]
  def getList(id: String, limit: Int, startIndex: Int, searchTerms: Array[String]): Option[Request]
  def shareObject(id: String, cascade: Boolean = false): Option[Request]

  def createDatafile(): Option[Request]

}

trait APIHelper {

  protected def pack(condition: Boolean = false, configuration: Configuration)(request: Request): Option[Request] =
    if (configuration.isIncomplete || condition) None
    else Some(request)

  // Hack -- TODO: Discuss
  def split(id: String, delimiter: String = "_") = {
    try {
      val pos = id.indexOf(delimiter)
      (id.substring(0, pos), id.substring(pos + 1))
    } catch {
      case _ => ("", "")
    }
  }

}

class DefaultAPI(config: Configuration) extends CallGenerator with APIHelper {

  private val configuration = config
  
  private val short_basis = :/(configuration.host, configuration.port) / configuration.prefix
  private val basis_data = short_basis / configuration.prefixData
  private val basis_metadata = short_basis / configuration.prefixMetaData

  private def pickBasis(objType: String) = objType match {
    case "section" => basis_metadata
    case "property" => basis_metadata
    case "value" => basis_metadata
    case _ => basis_data
  }

  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  def authenticateUser(username: String, password: String): Option[Request] =
    pack(false, configuration) {
      val post_body = "username=" + username + "&password=" + password
      (short_basis / "account" / "authenticate" / "" << post_body)
    }

  def createObject(objectType: String): Option[Request] =
    pack(objectType.isEmpty, configuration) { (pickBasis(objectType) / objectType / "").POST }

  def updateObject(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (pickBasis(split(id)._1) / split(id)._1 / split(id)._2 / "").POST
    }

  def getObject(id: String, full: Boolean = true): Option[Request] =
    pack(id.isEmpty, configuration) {
      val args = if (full) Map("q" -> "full") else Map()
      pickBasis(split(id)._1) / split(id)._1 / split(id)._2 / "" <<? args
    }

  def shareObject(id: String, cascade: Boolean = false): Option[Request] =
    pack(id.isEmpty, configuration) {
      val args = Map("cascade" -> (if (cascade) "1" else "0"))
      pickBasis(split(id)._1) / split(id)._1 / split(id)._2 / "acl" / "" <<? args
    }

  def getList(objectType: String, limit: Int, startIndex: Int, searchTerms: Array[String]): Option[Request] =
    pack(objectType.isEmpty, configuration) {
      val query = (for (term <- searchTerms) yield {
	val terms = split(term, "|")
	("%s".format(terms._1) -> terms._2)
      })
      (pickBasis(objectType) / objectType / "" <<? (Map("max_results" -> limit.toString,
				       "offset" -> startIndex.toString)) ++ query)
    }

  def createDatafile(): Option[Request] =
    pack(false, configuration) {
      (short_basis / "datafiles" / "").POST
    }
  
}
