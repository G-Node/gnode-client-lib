package org.gnode.lib.client

// Internal packages
import org.gnode.lib.cache._
import org.gnode.lib.util._
import org.gnode.lib.conf._
import org.gnode.lib.neo._
import org.gnode.lib.api._
import org.gnode.lib.parse._

// External packages
import dispatch._

// Make log messages available globally
import LogMessages._

class Authenticator(private val config: Configuration, private val http: Http) extends Loggable {

  lazy val caller = CallGenerator(config)
  var auth = false

  def authenticated[T](block: => T): T =
    if (!this.auth) {
      this.authenticate()
      block
    } else {
      block
    }
  
  def authenticate(): Boolean = {
    authenticate(config.username, config.password)
  }

  def authenticate(username: String, password: String): Boolean = {

    logger info AUTHENTICATE_BEGIN(username)
    
    val request = caller.authenticateUser(username, password) match {
      case Some(r) => r
      case _ => return false
    }

    try {

      val handler = request as_str

      val body = http(handler)
      auth = true
      return true

    } catch {

      case StatusCode(401, message) =>
	logger error AUTHENTICATE_FAILURE(username)
      case StatusCode(code, message) =>
	logger error HTTP_GENERAL(code, message)
      	logger error AUTHENTICATE_ERROR(username)
      case _ =>
	logger error AUTHENTICATE_ERROR(username)
      
    }

    auth = false
    return false

  }

}
