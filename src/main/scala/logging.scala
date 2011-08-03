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

  // Potentially problematic
  // TODO: Explore Option[T]!
  var logger: Logger = null

  /** To be called in constructor of subclass (usually with getClass.toString). Logging via
   * logger.error/debug/info/warn/fatal/(...). */

  def logInit(logNode: String = "",
	      logFile: String = "/home/aleonhardt/log/gnode/dev.log"): Unit = {

    val logConfig = new LoggerConfig {

      node = logNode
      level = Level.DEBUG

      handlers = new FileHandlerConfig {
	filename = logFile
	roll = Policy.Never
      }

    }

    logger = logConfig()

  } 

}
