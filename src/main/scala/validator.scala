package org.gnode.lib.validate

import org.gnode.lib.neo._
import org.gnode.lib.conf._
import org.gnode.lib.parse._
import org.gnode.lib.util._
import org.gnode.lib.util.File._
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
  def getAttributes(t: String) = this.contract(t).data_fields.toArray
  def getChildren(t: String) = this.contract(t).children.toArray
  def getParents(t: String) = this.contract(t).parents.toArray

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

    if (!contract.isDefinedAt(objectType)) throw new IllegalArgumentException
    
    // Negative check -- any required fields missing?
    contract(objectType).required forall { obj.isDefinedAt(_) }

  }

  // Object filter
  def filter(obj: NEObject, objectType: String): Option[NEObject] =
    None

}
