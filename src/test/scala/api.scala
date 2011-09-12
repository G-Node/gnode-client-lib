import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

import org.gnode.lib.conf._
import org.gnode.lib.api._

import dispatch._
import java.net.URI

class CallGeneratorSpec extends WordSpec with ShouldMatchers {

  private def checkURI(req: Request, scheme: String, host: String, path: String): Boolean = {
    val uri = request to_uri
    uri.getScheme == scheme && uri.getHost == host && uri.getPath == path
  }
  
  val config = ConfigurationReader.default
  val default_gen = new DefaultCallGenerator(config)
  val empty_gen = new DefaultCallGenerator(ConfigurationReader.create("", "", "", 0, "", ""))
    

  "authenticateUser() on CallGenerator" when {
    
    "called with specified username and password" should {
      
      val request = default_gen.authenticateUser("test", "pass").get
      
      "return a POST request" in { request.method should equal ("POST") }
      "have 'username=X&password=Y' as POST body" in {
	val post_body = org.apache.http.util.EntityUtils.toString(request.body.get)
	post_body should equal ("username=test&password=pass")
      }
      "correspond to a URI of the form http://host/account/authenticate/ (in accordance with configuration)" in {
	checkURI(request, "http", config.host, "/account/authenticate/") should be true
      }
	

    }

    "called with empty username and password but fully-defined configuration" should {

      val request = default_gen.authenticateUser().get

      "return a POST request" in { request.method should equal ("POST") }
      "have 'username=default&password=default' as POST body, with defaults from configuration" in {
	val post_body = org.apache.http.util.EntityUtils.toString(request.body.get)
	post_body should equal ("username=" + config.username + "&password=" + config.password)
      }
      "correspond to a URI of the form http://host/account/authenticate (in accordance with configuration)" in {
	checkURI(request, "http", config.host, "/account/authenticate") should be true
      }

    }

    "called without appropriate configuration" should {
      
      val request1 = empty_gen.authenticateUser()
      val request2 = empty_gen.authenticateUser("test", "pass")
      
      "return None with parameters" in {
	request2 should be (None)
      }

      "return None without parameters" in {
	request1 should be (None)
      }

    }

  }

  "createObject() on CallGenerator" when {

    "called" should {

      val request = default_gen.createObject()
      
      "return a PUT request" in { (req method) should equal ("PUT") }
      "produce URI corresponding to to http://host/neo/" in {
	checkURI(request, "http", config.host, "/neo/") should be true
      }

    }

    "called without configuration" should {

      val request = empty_gen.createObject()
      "return None" in { request should be None }

    }

  }

}
