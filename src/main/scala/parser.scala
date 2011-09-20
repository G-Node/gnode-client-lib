package org.gnode.lib.parse

import org.gnode.lib.neo._
import org.gnode.lib.util._
import org.gnode.lib.api._

import net.liftweb.json._
import net.liftweb.json.JsonParser.ParseException

import dispatch._

import org.gnode.lib.conf.LogMessages._

class ExtractError extends Exception("NEO object could not be parsed")

object Writer extends Loggable {

}

object Reader extends Loggable {
  
  def apply(data: String) = makeObject(data)

  def makeListOpt(data: String): Option[List[String]] = {

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

    val parsedData = p get

    return Some(for { JField("selected", JArray(list)) <- parsedData
		       JString(value) <- list } yield value)

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

    val parsedData = p get

    val strMap = MuMap[String, String]()
    val numMap = MuMap[String, Double]()
    val dataMap = MuMap[String, NEOData]()

    def isData(l: List[JField]) =
      l exists { f: JField => f.name == "units" }

    def notData(l: List[JField]) =
      !isData(l)
     
    // Extract string-based info
    for {
      JObject(list) <- parsedData
      if notData(list)
      JField(key, JString(value)) <- list
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

    // Extract data
    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JDouble(data))))) <- parsedData
    } dataMap += key -> new NEOData(units, data)

    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JInt(data))))) <- parsedData
    } dataMap += key -> new NEOData(units, data.toDouble)

    for {
      JField(key, JObject(List(JField("units", JString(units)), JField("data", JArray(data))))) <- parsedData
    } {
      
      val buffer = ListBuffer[Double]()
      for (JDouble(d) <- data) buffer += d
      dataMap += key -> new NEOData(units, buffer.toList)
    
    }
    
    // Build return object
    Some(new NEObject(Map.empty ++ strMap, Map.empty ++ numMap, Map.empty ++ dataMap))
    
  }
    
}