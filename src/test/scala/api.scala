import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

import org.gnode.lib.conf._
import org.gnode.lib.api._

import dispatch._

class CallGeneratorSpec extends WordSpec with ShouldMatchers {

  "authenticateUser() on CallGenerator" when {
    
    val config = ConfigurationReader.default
    val gen = new DefaultCallGenerator(config)
    
    "called with specified username and password" should {

      val request = gen.authenticateUser("test", "pass").get
      
      "return a POST request" in {
	request.method should equal ("POST")
      }
      
      "have 'username=X&password=Y' as POST body" in {
	val post_body = org.apache.http.util.EntityUtils.toString(request.body.get)
	post_body should equal ("username=test&password=pass")
      }
	
      "correspond to a URI of the form http://host/account/authenticate (in accordance with configuration)" in {
	val uri = request to_uri
	(uri getScheme) should equal ("http")
	(uri getHost) should equal (config host)
	(uri getPath) should equal ("/account/authenticate")
      }
	

    }

    "called with empty username and password but fully-defined configuration" should {

      val request = gen.authenticateUser().get

      "return a POST request" in {
	request.method should equal ("POST")
      }

      "have 'username=default&password=default' as POST body, with defaults from configuration" in {
	val post_body = org.apache.http.util.EntityUtils.toString(request.body.get)
	post_body should equal ("username=" + config.username + "&password=" + config.password)
      }

      "correspond to a URI of the form http://host/account/authenticate (in accordance with configuration)" in {
	val uri = request to_uri
	(uri getScheme) should equal ("http")
	(uri getHost) should equal (config host)
	(uri getPath) should equal ("/account/authenticate")
      }

    }

    "called without appropriate configuration" should {
      
      val gen2 = new DefaultCallGenerator(ConfigurationReader.create("", "", "", 0, "", ""))
      val request1 = gen2.authenticateUser()
      val request2 = gen2.authenticateUser("test", "pass")
      
      "return None with parameters" in {
	request2 should be (None)
      }

      "return None without parameters" in {
	request1 should be (None)
      }

    }

  }

}
      
