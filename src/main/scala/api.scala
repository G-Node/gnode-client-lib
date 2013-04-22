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

// Singleton object generating API generator from configuration:

object CallGenerator {
  def apply(config: Configuration): CallGenerator = new DefaultAPI(config)
}

// Outline for API:

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

// APIHelper encapsulates a number of minor helper functions:

trait APIHelper {

  // This one's a little tricky: pack() is a curried function that takes certain
  // failure conditions and a configuration, and produces a function that emits
  // either the supplied request or, if the failure condition is MET, None. This
  // circumvents long-winded if-clauses in the API clauses.
  
  protected def pack(condition: Boolean = false, configuration: Configuration)(request: Request): Option[Request] =
    if (configuration.isIncomplete || condition) None
    else Some(request)

  // split() turns the client ID representation (e.g., "analogsignal_947") into
  // the corresponding type/ID tuple (e.g., "analogsignal" and 947). This is
  // obviously a fairly fragile construction. Requires discussion/amending.
  
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

  // We retain the configuration and turn it private:
  private val configuration = config

  // Short basis is used for, e.g., authentication (e.g., portal.g-node.org:80/):
  private val short_basis = :/(configuration.host, configuration.port) / configuration.prefix
  // Data basis (e.g., portal.g-node.org:80/electrophysiology/):
  private val basis_data = short_basis / configuration.prefixData
  // Metadata basis (e.g., portal.g-node.org:80/metadata/):
  private val basis_metadata = short_basis / configuration.prefixMetaData

  // Given that there's no clear distinction client-side between a DATA object
  // and a METADATA object, we have to distinguish. TODO: Push this hard-coded
  // definition into requirements.json.
  
  private def pickBasis(objType: String) = objType match {
    case "section" => basis_metadata
    case "property" => basis_metadata
    case "value" => basis_metadata
    case _ => basis_data
  }

  // In case of username/password from configuration:
  def authenticateUser(): Option[Request] = authenticateUser(configuration.username,
							     configuration.password)

  // AUTHENTICATION: This generates the call corresponding to authentication based on
  // username and password. There are no failure conditions (only credentials required).
  def authenticateUser(username: String, password: String): Option[Request] =
    pack(false, configuration) {
      val post_body = "username=" + username + "&password=" + password
      (short_basis / "account" / "authenticate" / "" << post_body)
    }

  // OBJECT CREATION
  def createObject(objectType: String): Option[Request] =
    pack(objectType.isEmpty, configuration) { (pickBasis(objectType) / objectType / "").POST }

  // OBJECT UPDATE
  def updateObject(id: String): Option[Request] =
    pack(id.isEmpty, configuration) {
      (pickBasis(split(id)._1) / split(id)._1 / split(id)._2 / "").POST
    }

  // OBJECT RETRIEVAL: ID is unpacked according to pack() function; this is, of course,
  // not ideal but required by the current UI design. "full" parameter may be deprecated
  // under new HDF5 scheme.
  def getObject(id: String, full: Boolean = true): Option[Request] =
    pack(id.isEmpty, configuration) {
      val args = if (full) Map("q" -> "full") else Map()
      pickBasis(split(id)._1) / split(id)._1 / split(id)._2 / "" <<? args
    }

  // OBJECT SHARING
  def shareObject(id: String, cascade: Boolean = false): Option[Request] =
    pack(id.isEmpty, configuration) {
      val args = Map("cascade" -> (if (cascade) "1" else "0"))
      pickBasis(split(id)._1) / split(id)._1 / split(id)._2 / "acl" / "" <<? args
    }

  // LIST RETRIEVAL: This includes SEARCH, via searchTerms. Currently, this is
  // not configurable; search always implicitly assumes that we're doing a case-
  // insensitive look-up for all supplied terms. No boolean search logic or
  // similar.
  def getList(objectType: String, limit: Int, startIndex: Int, searchTerms: Array[String]): Option[Request] =
    pack(objectType.isEmpty, configuration) {
      val query = (for (term <- searchTerms) yield {
	val terms = split(term, "|")
	("%s__icontains".format(terms._1) -> terms._2)
      })
      (pickBasis(objectType) / objectType / "" <<? (Map("max_results" -> limit.toString,
				       "offset" -> startIndex.toString)) ++ query)
    }

  // DATAFILE UPLOAD: NB, datafile download is not handled here but in the
  // MATLAB utilities. This is less than ideal.
  def createDatafile(): Option[Request] =
    pack(false, configuration) {
      (short_basis / "datafiles" / "").POST
    }
  
}
