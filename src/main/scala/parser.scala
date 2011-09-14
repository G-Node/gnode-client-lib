package org.gnode.lib.parse

import org.gnode.lib.neo._
import org.gnode.lib.util._
import org.gnode.lib.api._

import net.liftweb.json._
import net.liftweb.json.JsonParser.ParseException
import dispatch._

object NEOParser extends Loggable {
  
  def apply(data: String) = make(data)

  def make(data: String): NEObject =
    makeOpt(data).getOrElse(null)
  
  def makeOpt(data: String): Option[NEObject] = {
    
    import scala.collection.mutable.{ListBuffer, Map => MuMap}

    val parsedData = parse(data)

    val strMap = MuMap[String, String]()
    val numMap = MuMap[String, Double]()
    val dataMap = MuMap[String, NEOData]()

    def notData(l: List[JField]): Boolean =
      !(l exists { f: JField => f.name == "units" })

    def isData(l: List[JField]) = !notData(l)

    // Extract string-based info
    for {JObject(list) <- parsedData
	 if notData(list)
	 JField(key, JString(value)) <- list} { strMap += key -> value }

    // Extract double- and integer-based info
    for {JObject(list) <- parsedData
	 if notData(list)
	 JField(key, JInt(value)) <- list} { numMap += key -> value.toDouble }

    for {JObject(list) <- parsedData
	 if notData(list)
	 JField(key, JDouble(value)) <- list} { numMap += key -> value }

    // Extract data
    for {JField(key, JObject(List(JField("units", JString(units)), JField("data", JDouble(data))))) <- parsedData}
      { dataMap += key -> new NEOData(units, data) }

    for {JField(key, JObject(List(JField("units", JString(units)), JField("data", JInt(data))))) <- parsedData}
      { dataMap += key -> new NEOData(units, data.toDouble) }

    for {JField(key, JObject(List(JField("units", JString(units)), JField("data", JArray(data))))) <- parsedData}
      {
	val buffer = ListBuffer[Double]()
	for (JDouble(d) <- data) buffer += d

	dataMap += key -> new NEOData(units, buffer.toList)
      }
    
    // Build return object
    Some(new NEObject(Map.empty ++ strMap, Map.empty ++ numMap, Map.empty ++ dataMap))
    
  }
    
}
