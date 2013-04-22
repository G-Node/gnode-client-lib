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

// This package provides all JSON parsing-related functionality
// (including both a Writer and a Reader; renaming of package and/or
// file may be in order here!). Writer takes a NEObject and outputs a
// valid JSON string.  Reader performs the reverse, and emits
// appropriate Java objects from supplied JSON strings (e.g., object
// list to array, or object to NEObject).
//
// Naturally, no HTTP handling is done here.

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

  // Serializer needs specific information about the case classes we
  // want auto-serialized:
  
  implicit val formats = DefaultFormats + FieldSerializer[NEODataSingle]() + FieldSerializer[NEODataMulti]() + FieldSerializer[NEODataURL]()

  // pretty() may not be appropriate, as it's wasteful, but the
  // additional \n's and \t's help with checking and
  // debugging. Alternative: compact().
  
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

  // Emits object lists
  
  def makeListOpt(data: String): Option[List[List[String]]] = {

    var p: Option[JValue] = None
    
    try {
      
      // Check if we can get a basic parse:
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

    // The following block is rather inelegant, but currently
    // required. In the list JSON, we are only guaranteed a permalink,
    // but neither name nor description (which is, semantically, a bit
    // weird). In order to handle this case, the following parsing
    // code handles all four cases separately. I'm virtually certain
    // that one should suffice, but the expression for optionally
    // EMPTY fields eludes me currently.

    // Both name and description:
    val both = for {
      JObject(obj) <- parsedData
      JField("permalink", JString(permalink)) <- obj
      JField("fields", JObject(fields)) <- obj
      JField("name", JString(name)) <- fields
      JField("description", JString(description)) <- fields
    } yield List(extractID(permalink), name, description)

    // Description not available:
    val no_desc = for {
      JObject(obj) <- parsedData
      JField("permalink", JString(permalink)) <- obj
      JField("fields", JObject(fields)) <- obj
      JField("name", JString(name)) <- fields
      JField("description", JNull) <- fields
    } yield List(extractID(permalink), name, "")
    
    // Name not available:
    val no_name = for {
      JObject(obj) <- parsedData
      JField("permalink", JString(permalink)) <- obj
      JField("fields", JObject(fields)) <- obj
      JField("name", JNull) <- fields
      JField("description", JString(description)) <- fields
    } yield List(extractID(permalink), "", description)

    // Missing name and description:
    val none = for {
      JObject(obj) <- parsedData
      JField("permalink", JString(permalink)) <- obj
      JField("fields", JObject(fields)) <- obj
      JField("name", JNull) <- fields
      JField("description", JNull) <- fields
    } yield List(extractID(permalink), "", "")

    return Some(both ++ no_desc ++ no_name ++ none)

  }

  // Cheap wrapper in case we're not interested in Option:

  def makeObject(data: String): NEObject =
    makeObjectOpt(data) match {
      case Some(n) => n
      case None => throw new ExtractError
    }

  // This function takes JSON and emits a NEObject. A lot of edge case
  // handling occurs here; ideally, that's all represented in
  // requirements.json.
  
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

    // Hard-coded:
    val parsedData = ((p get) \ "selected") \ "fields"

    // Prepare mutable containers for all expected item types
    val strMap = MuMap[String, String]()
    val numMap = MuMap[String, Double]()
    val dataMap = MuMap[String, NEOData]()
    val relMap = MuMap[String, Array[String]]()

    // HELPER FUNCTION: Checks if we're dealing with a data field, which is
    // detected implicitly (via existence of "units").
    
    def isData(l: List[JField]) =
      l exists { f: JField => f.name == "units" }

    def notData(l: List[JField]) =
      !isData(l)

    // HELPER FUNCTION: All relationship fields need to be handled
    // differently, but we can only identify them based on hard-coded
    // features. This should be based on information in
    // requirements.json, and not hard-coded. Currently NOT
    // implemented.
    
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
     
    // Extract string-based info:
    
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JString(value)) <- list
      if notRelation(key)
    } {
      strMap += key -> value
    }

    // Extract double- and integer-based info:
    
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

    // Extract data about relationships:
    
    import org.gnode.lib.util.IDExtractor._

    // Several:
    
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JArray(rels)) <- list
    } {

      val buffer = ListBuffer[String]()
      for (JString(rel) <- rels) buffer += extractID(rel)
      relMap += key -> buffer.toArray

    }

    // One:
    
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JString(value)) <- list
      if isRelation(key)
    } relMap += key -> Array(extractID(value))

    // Extract data:
    
    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JDouble(data))))) <- parsedData
    } dataMap += key -> new NEODataSingle(units, data)

    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JInt(data))))) <- parsedData
    } dataMap += key -> new NEODataSingle(units, data.toDouble)

    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JString(url))))) <- parsedData
    } dataMap += key -> new NEODataURL(units, url)

    // Add id from permalink: This is, of course, problematic; we're in need
    // of a more reasonable solution (e.g., just integers etc.) here.
    
    strMap += "id" -> (for {
      JField("permalink", JString(value)) <- ((p get) \ "selected")
    } yield extractID(value)).head
    
    // Build return object:
    
    Some(new NEObject(Map.empty ++ strMap, Map.empty ++ numMap, Map.empty ++ dataMap, Map.empty ++ relMap))
    
  }
    
}
