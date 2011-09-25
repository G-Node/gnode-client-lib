package org.gnode.lib.neo

sealed abstract class NEOData {
  def getUnits: String
  def getData: Array[Double]
  def toString: String
}

case class NEODataSingle(units: String,
			 data: Double) extends NEOData {
 
  def getUnits = this.units
  def getData = Array(this.data)
  override def toString = "%s %s".format(this.data, this.units)

}

case class NEODataMulti(units: String,
			data: Array[Double]) extends NEOData {

  def getUnits = this.units
  def getData = this.data
  override def toString = "%s (...) %s".format(this.data.head, this.units)

}

class NEObject(val stringInfo: Map[String, String] = Map[String, String](),
	       val numInfo: Map[String, Double] = Map[String, Double](),
	       val data: Map[String, NEOData] = Map[String, NEOData](),
	       val relations: Map[String, Array[String]] = Map[String, Array[String]]()) {

  def getStringKeys =
    stringInfo.keys.toArray

  def getNumKeys =
    numInfo.keys.toArray

  def getDataKeys =
    data.keys.toArray

  def getRelKeys =
    relations.keys.toArray

  def isDefinedAt(key: String) =
    (stringInfo.isDefinedAt(key) || numInfo.isDefinedAt(key) || data.isDefinedAt(key) || relations.isDefinedAt(key))

  def pretty = (
    (for ((k, v) <- stringInfo) yield "%s: %s".format(k, v)).toList :::
    (for ((k, v) <- numInfo) yield "%s: %s".format(k, v.toString)).toList :::
    (for ((k, v) <- data) yield "%s: %s".format(k, v.toString)).toList :::
    (for ((k, v) <- relations) yield "%s: %s".format(k, v.mkString(", "))).toList
    ).mkString("\n")

}

class NEOBuilder {

  import collection.mutable.{Map => MuMap}

  private val strings = MuMap[String, String]()
  private val numbers = MuMap[String, Double]()
  private val data = MuMap[String, NEOData]()
  private val rel = MuMap[String, Array[String]]()

  def add(k: String, v: String) { strings += k -> v }
  def add(k: String, v: Double) { numbers += k -> v }
  def add(k: String, v: NEOData) { data += k -> v }
  def add(k: String, v: Array[String]) { rel += k -> v }

  def build: NEObject =
    new NEObject(Map.empty ++ strings, Map.empty ++ numbers, Map.empty ++ data, Map.empty ++ rel)

}
