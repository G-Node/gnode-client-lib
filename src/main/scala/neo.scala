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

package org.gnode.lib.neo

// Basic abstract data container, as data comes in different varieties
// that need to be handled accordingly (e.g., intrinsic arrays,
// URL-mediated, etc.).

sealed abstract class NEOData {
  def getUnits: String
  def getData: Array[Double]
  def getURL: String
  def toString: String
}

// NEODataSingle: Represents a single numerical data field, such as
// permission level or similar.

case class NEODataSingle(units: String,
			 data: Double) extends NEOData {
 
  def getUnits = this.units
  def getData = Array(this.data)
  def getURL = ""
  override def toString = "%s %s".format(this.data, this.units)

}

// NEODataURL: Represents a HDF5-based data field, in which the URL
// points to a valid HDF5 file containing the associated multi- or
// one-dimensional array. Data handling is handled on the user-facing
// client side (e.g., MATLAB).

case class NEODataURL(units: String,
		      url: String) extends NEOData {

  def getUnits = this.units
  def getData = Array[Double]()
  def getURL = this.url
  override def toString = "At: %s in %s".format(this.url, this.units)

}

case class NEODataMulti(units: String,
			data: String) extends NEOData {

  def getUnits = this.units
  def getData = Array[Double]()
  def getURL = this.data
  override def toString = "Data location: %s".format(this.data)

}

// NEObject: Basic object container. Represents the full JSON object,
// and can be built step-wise (e.g., from MATLAB) by using
// NEOBuilder. Given that we're in a heavily typed environment, this
// handling is necessary -- could be simplified by using
// content-neutral element objects.

class NEObject(val stringInfo: Map[String, String] = Map[String, String](),
	       val numInfo: Map[String, Double] = Map[String, Double](),
	       val data: Map[String, NEOData] = Map[String, NEOData](),
	       val relations: Map[String, Array[String]] = Map[String, Array[String]]()) {

  // Utility functions that allow access to stored items and elements.
  
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

  // For command line use, this function allows pretty display of all
  // elements regardless of type.
  
  def pretty = (
    (for ((k, v) <- stringInfo) yield "%s: %s".format(k, v)).toList :::
    (for ((k, v) <- numInfo) yield "%s: %s".format(k, v.toString)).toList :::
    (for ((k, v) <- data) yield "%s: %s".format(k, v.toString)).toList :::
    (for ((k, v) <- relations) yield "%s: %s".format(k, v.mkString(", "))).toList
    ).mkString("\n")

}

// NEOBuilder: Generator class for NEObject. Allows step-wise build of NEObjects
// from, e.g., MATLAB; by calling build(), we get a full object representation
// that can be uploaded.

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
