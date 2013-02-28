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

package org.gnode.lib.util

import com.twitter.logging.{Logger, Level, Policy}
import com.twitter.logging.config._
import java.io.File

/** Convenience trait for logging via Twttr's '''util-logger''' utility
 *
 * Mix-in of this trait provides `logger` member which implements
 * all functionality provided by util-logger; currently, configuration
 * only in `Loggable` source.
 */

trait Loggable {

  var logNode = (this getClass) toString
  var logFile = File.createTempFile("gnode", ".log").getPath
  //var logFile = "/tmp/gnode.log"

  lazy val logger: Logger =
    new LoggerConfig {

      node = logNode
      level = Level.DEBUG

      handlers = new FileHandlerConfig {
	filename = logFile
	roll = Policy.Never
      }

    } apply

}
