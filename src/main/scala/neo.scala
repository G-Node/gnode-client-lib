package org.gnode.lib.neo

sealed abstract class NEOData {
  def getUnits: String
  def getData: List[Double]
}

case class NEODataSingle(units: String,
			 data: Double) extends NEOData {
 
  def getUnits: String = this.units
  def getData: List[Double] = List(this.data)

}

case class NEODataMulti(units: String,
		   data: List[Double]) extends NEOData {

  def getUnits: String = this.units
  def getData: List[Double] = this.data

}

class NEObject(val stringInfo: Map[String, String] = Map[String, String](),
	       val numInfo: Map[String, Double] = Map[String, Double](),
	       val data: Map[String, NEOData] = Map[String, NEOData](),
	       val relations: Map[String, List[String]] = Map[String, List[String]]()) {

  def isDefinedAt(key: String) =
    (stringInfo.isDefinedAt(key) || numInfo.isDefinedAt(key) || data.isDefinedAt(key) || relations.isDefinedAt(key))

}

class NEOBuilder {

  import collection.mutable.{Map => MuMap}

  private val strings = MuMap[String, String]()
  private val numbers = MuMap[String, Double]()
  private val data = MuMap[String, NEOData]()
  private val rel = MuMap[String, List[String]]()

  def add(k: String, v: String) { strings += k -> v }
  def add(k: String, v: Double) { numbers += k -> v }
  def add(k: String, v: NEOData) { data += k -> v }
  def add(k: String, v: List[String]) { rel += k -> v }

  def build: NEObject =
    new NEObject(Map.empty ++ strings, Map.empty ++ numbers, Map.empty ++ data, Map.empty ++ rel)

}