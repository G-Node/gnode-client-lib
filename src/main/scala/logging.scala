package org.gnode.lib.util

import com.twitter.logging.{Logger, Level, Policy}
import com.twitter.logging.config._

// CONVENIENCE TRAIT FOR LOGGING VIA TWITTER/UTIL-LOGGER

trait Loggable {

  // Potentially problematic
  // TODO: Explore Option[T]!
  var logger: Logger = null

  // Called from extended class before first log; provides logger: Logger
  def logInit(logNode: String = "", logFile: String = "/home/aleonhardt/log/gnode/dev.log"): Unit = {

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
