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