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

package org.gnode.lib.validate

import org.gnode.lib.neo._
import org.gnode.lib.conf._
import org.gnode.lib.parse._
import org.gnode.lib.util._
import org.gnode.lib.util.FileUtil._
import org.gnode.lib.api.APIHelper

import net.liftweb.json._
import scala.collection.mutable.Map

import org.gnode.lib.conf.LogMessages._

case class Contract(required: List[String],
		    attributes: List[String],
		    data_fields: List[String],
		    children: List[String],
		    parents: List[String])

class Validator(private val config: Configuration) extends Loggable {
		      
  private val contract: Map[String, Contract] =
    readContractFromFile(config.apiDefinition).getOrElse(Map[String, Contract]())

  // Contract access
  def getRequired(t: String) = this.contract(t).required.toArray

  def getData(t: String) = this.contract(t).data_fields.toArray
  def getAttributes(t: String) = this.contract(t).attributes.toArray
  def getChildren(t: String) = this.contract(t).children.toArray
  def getParents(t: String) = this.contract(t).parents.toArray

  def getAll(t: String) = if (contract.isDefinedAt(t)) getData(t) ++ getAttributes(t) ++ getChildren(t) ++ getParents(t) else Array[String]()

  // CONTRACT EXTRACTION METHODS
  private def readContractFromFile(path: String): Option[Map[String, Contract]] =
    try {
      using(io.Source.fromFile(path)) { file =>
	readContract(file.mkString) }
    } catch {
      case e: java.io.FileNotFoundException =>
	logger error FILE_NOT_FOUND(path)
	None
    }

  private def readContract(raw: String): Option[Map[String, Contract]] = {
    
    import net.liftweb.json.JsonParser.ParseException
    implicit val formats = DefaultFormats

    var p: Option[JValue] = None

    try {
      p = Some(parse(raw))
    } catch {
      case e: ParseException =>
	logger error READ_ERROR_PARSE
	return None
      case _ =>
	logger error READ_ERROR_UNKNOWN
	return None
    }

    val reqs = p get

    val rmap = for {
      JField(t, defs @ JObject(_)) <- reqs
    } yield t -> defs.extract[Contract]

    return Some(Map(rmap: _*))

  }

  // VALIDATION METHODS
  def validate(obj: NEObject): Boolean =
    // Attempt to guess object type
    if (obj.isDefinedAt("neo_id")) {

      val t = (new APIHelper {}).split(obj.stringInfo("neo_id"))._1
      validate(obj, t)

    } else if (obj.isDefinedAt("obj_type")) {

      val t = obj.stringInfo("obj_type")
      validate(obj, t)

    } else {

      throw new IllegalArgumentException

    }

  def validate(obj: NEObject, objectType: String): Boolean = {

    // CAREFUL: Unknown object types lead to trivially valid NEObjects
    if (!contract.isDefinedAt(objectType)) return true
    
    // Negative check -- any required fields missing?
    contract(objectType).required forall { obj.isDefinedAt(_) }

  }

  // Object filter
  def filter(obj: NEObject, objectType: String): Option[NEObject] =
    None

}
