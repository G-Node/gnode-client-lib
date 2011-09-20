package org.gnode.lib.neo

case class NEOData(units: String,
		   values: List[Double]) {

  def this(units: String, data: Double) = this(units, List(data))

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