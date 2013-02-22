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

package org.gnode.lib.parse

import org.gnode.lib.neo._
import org.gnode.lib.util._
import org.gnode.lib.api._
import org.gnode.lib.validate._

import net.liftweb.json._
import net.liftweb.json.JsonParser.ParseException

import dispatch._

import org.gnode.lib.conf.LogMessages._

class ExtractError extends Exception("NEO object could not be parsed")

object Writer extends Loggable {

  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.Extraction._
  import net.liftweb.json.Printer._

  import org.gnode.lib.util.IDExtractor._

  implicit val formats = DefaultFormats + FieldSerializer[NEODataSingle]() + FieldSerializer[NEODataMulti]() + FieldSerializer[NEODataURL]()

  def serialize(obj: NEObject): Option[String] =
    Some(pretty(render(
      decompose(obj.stringInfo) merge
      decompose(obj.numInfo) merge
      decompose(implodeID(obj.relations.filterNot { case (key, list) => list.isEmpty })) merge
      decompose(obj.data)
    )))
      
}

object Reader extends Loggable {

  import org.gnode.lib.util.IDExtractor._
  
  def makeListOpt(data: String): Option[List[List[String]]] = {

    var p: Option[JValue] = None
    
    try {
      p = Some(parse(data))
    } catch {
      case e: ParseException =>
	logger error READ_ERROR_PARSE
	return None
      case _ =>
	logger error READ_ERROR_UNKNOWN
	return None
    }

    val parsedData = (p get) \ "selected"

    return Some(for {
      JObject(obj) <- parsedData
      JField("permalink", JString(permalink)) <- obj
      JField("fields", JObject(fields)) <- obj
      JField("name", JString(name)) <- fields
      JField("description", JString(description)) <- fields
    } yield List(extractID(permalink), name, description))

  }

  def makeObject(data: String): NEObject =
    makeObjectOpt(data) match {
      case Some(n) => n
      case None => throw new ExtractError
    }
  
  def makeObjectOpt(data: String): Option[NEObject] = {
    
    import scala.collection.mutable.{ListBuffer, Map => MuMap}

    var p: Option[JValue] = None
    
    try {
      p = Some(parse(data))
    } catch {
      case e: ParseException =>
	logger error READ_ERROR_PARSE
	return None
      case _ =>
	logger error READ_ERROR_UNKNOWN
	return None
    }

    val parsedData = ((p get) \ "selected") \ "fields"

    val strMap = MuMap[String, String]()
    val numMap = MuMap[String, Double]()
    val dataMap = MuMap[String, NEOData]()
    val relMap = MuMap[String, Array[String]]()

    def isData(l: List[JField]) =
      l exists { f: JField => f.name == "units" }

    def notData(l: List[JField]) =
      !isData(l)

    def isRelation(label: String) = {
      label == "segment" ||
      label == "block" ||
      label == "event" ||
      label == "eventarray" ||
      label == "epoch" ||
      label == "epocharray" ||
      label == "unit" ||
      label == "spiketrain" ||
      label == "analogsignal" ||
      label == "analogsignalarray" ||
      label == "irsaanalogsignal" ||
      label == "spike" ||
      label == "recordingchannelgroup" ||
      label == "recordingchannel" ||
      label == "recordingchannel_set" ||
      label == "section" ||
      label == "parent_property" ||
      label == "parent_section" ||
      label == "metadata" ||
      label == "epocharray_set" ||
      label == "irsaanalogsignal_set" ||
      label == "analogsignal_set" ||
      label == "epoch_set" ||
      label == "eventarray_set" ||
      label == "analogsignalarray_set" ||
      label == "spiketrain_set" ||
      label == "spike_set" ||
      label == "event_set" ||
      label == "segment_set" ||
      label == "section_set" ||
      label == "datafile_set" ||
      label == "block_set" ||
      label == "property_set" ||
      label == "value_set"
    }

    def notRelation(label: String) =
      !isRelation(label)
     
    // Extract string-based info
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JString(value)) <- list
      if notRelation(key)
    } {
      strMap += key -> value
    }

    // Extract double- and integer-based info
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JInt(value)) <- list
    } numMap += key -> value.toDouble

    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JDouble(value)) <- list
    } numMap += key -> value

    // Extract data about relationships
    import org.gnode.lib.util.IDExtractor._

    // Several
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JArray(rels)) <- list
    } {

      val buffer = ListBuffer[String]()
      for (JString(rel) <- rels) buffer += extractID(rel)
      relMap += key -> buffer.toArray

    }

    // One
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JString(value)) <- list
      if isRelation(key)
    } relMap += key -> Array(extractID(value))

    // Extract data
    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JDouble(data))))) <- parsedData
    } dataMap += key -> new NEODataSingle(units, data)

    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JInt(data))))) <- parsedData
    } dataMap += key -> new NEODataSingle(units, data.toDouble)

    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JString(url))))) <- parsedData
    } dataMap += key -> new NEODataURL(units, url)

    // for {
    //   JField(key, JObject(List(JField("units", JString(units)), JField("data", JArray(data))))) <- parsedData
    // } {
      
    //   val buffer = ListBuffer[Double]()
    //   for (JDouble(d) <- data) buffer += d
    //   dataMap += key -> new NEODataMulti(units, buffer.toArray)
    
    // }

    // HACK: Add id from permalink
    strMap += "id" -> (for {
      JField("permalink", JString(value)) <- ((p get) \ "selected")
    } yield extractID(value)).head
    
    // Build return object
    Some(new NEObject(Map.empty ++ strMap, Map.empty ++ numMap, Map.empty ++ dataMap, Map.empty ++ relMap))
    
  }
    
}
