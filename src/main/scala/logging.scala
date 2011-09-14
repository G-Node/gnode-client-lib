package org.gnode.lib.util

import com.twitter.logging.{Logger, Level, Policy}
import com.twitter.logging.config._

/** Convenience trait for logging via Twttr's '''util-logger''' utility
 *
 * Mix-in of this trait provides `logger` member which implements
 * all functionality provided by util-logger; currently, configuration
 * only in `Loggable` source.
 */

trait Loggable {

  var logNode = (this getClass) toString
  var logFile = "/tmp/gnode.log"

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
