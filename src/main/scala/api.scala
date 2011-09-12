package org.gnode.lib.api

import dispatch._
import org.gnode.lib.conf._

trait CallGenerator {

  def authenticateUser(): Request
  def authenticateUser(username: String, password: String): Request

}

class DefaultCallGenerator(val configuration: Configuration) extends CallGenerator {

  def authenticateUser(): Request = authenticateUser(configuration.username,
						     configuration.password)

  def authenticateUser(username: String, password: String): Request = {

    val basis = :/(configuration.host)
    val post_body = "username=" + username + "&password=" + password
    
    basis / "login" / "authenticate" / "" << post_body

  }

}
