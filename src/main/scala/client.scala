package org.gnode.lib.client

import org.gnode.lib.cache._
import org.gnode.lib.util._
import org.gnode.lib.conf._

import dispatch._

trait HttpInteractor extends Loggable {
  
  // Create custom Http object that uses library-wide Twttr logging
  lazy val http = new Http with Loggable {
    override def make_logger = new Logger {

      def info(msg: String, items: Any*) { logger.info(msg, items: _*) }
      def warn(msg: String, items: Any*) { logger.warning(msg, items: _*) }
      
    }
  }

}

class TransactionManager(config: Configuration) extends HttpInteractor {

  // Call generation
  import org.gnode.lib.api._

  val d = new Downloader
  val u = new Uploader
  
}

class Downloader
class Uploader
