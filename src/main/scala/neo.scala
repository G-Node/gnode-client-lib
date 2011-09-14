package org.gnode.lib.neo

case class NEOData(units: String, values: List[Double]) {
  def this(units: String, data: Double) = this(units, List(data))
}

class NEObject(val stringInfo: Map[String, String], val numInfo: Map[String, Double], val data: Map[String, NEOData])
