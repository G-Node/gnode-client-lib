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

package org.gnode.lib.conf

import org.gnode.lib.util.Loggable
import org.gnode.lib.util.File._

// Encapsulates configuration data. Emitted by ConfigurationReader;
// accepted by Connector.
case class Configuration(username: String,
			 password: String,
			 host: String,
			 port: Int,
			 prefix: String,
			 prefixData: String,
			 prefixMetaData: String,
			 apiDefinition: String,
			 caching: String,
			 db: String) {

  def isIncomplete(): Boolean =
    host.isEmpty || port == 0 || username.isEmpty

}
    
object ConfigurationReader extends Loggable {
  
  /** Parameterized configuration generator. */
  def create(username: String, 
	     password: String,
	     host: String,
	     port: Int,
	     prefix: String,
	     prefixData: String,
	     prefixMetaData: String,
	     apiDefinition: String,
	     caching: String,
	     db: String) = Configuration(username, password, host, port, prefix, prefixData, prefixMetaData, apiDefinition, caching, db)

  /** Wrapper around fromString for straightforward configuration loading from file */
  def fromFile(filename: String): Option[Configuration] = {

    try {

      using(io.Source.fromFile(filename)) { file =>
	fromString(file.mkString)
      }

    } catch {

      case e: java.io.FileNotFoundException => {
	logger.error("Configuration file (" + filename + ") not found")
	None
      }
      case _ => {
	logger.error("Error while importing from file (" + filename + ")")
	None
      }

    }

  }

  /** Creates configuration object from JSON string. */
  def fromString(config: String): Option[Configuration] = {

    import net.liftweb.json._
    implicit val formats = DefaultFormats

    try {

      val json = parse(config)
      return Some(json.extract[Configuration])

    } catch {

      case e => {
	logger.error("Parsing error: " + e)
	None
      }

    }

  }
    
}
