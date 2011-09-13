import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

import org.gnode.lib.conf._
import org.gnode.lib.api._

import dispatch._
import java.net.URI

class CallGeneratorSpec extends WordSpec with ShouldMatchers {

  private def checkURI(request: Request, scheme: String, host: String, path: String, query: String = null): Boolean = {
    val uri = request to_uri
    val (s, h, p, q) = (uri.getScheme, uri.getHost, uri.getPath, uri.getQuery)
    s == scheme && h == host && p == path && q == query
  }
  
  private val config = ConfigurationReader.default
  private val default_gen: CallGenerator = new DefaultAPI(config)
  private val empty_gen: CallGenerator = new DefaultAPI(ConfigurationReader.create("", "", "", 0, "", ""))

  "authenticateUser() on CallGenerator" when {    
    "called with specified username and password" should {
      
      val request = default_gen.authenticateUser("test", "pass").get
      
      "return a POST request" in { request.method should equal ("POST") }

      "have 'username=X&password=Y' as POST body" in {
	val post_body = org.apache.http.util.EntityUtils.toString(request.body.get)
	post_body should equal ("username=test&password=pass")
      }

      "correspond to a URI of the form http://host/account/authenticate/ (in accordance with configuration)" in {
	checkURI(request, "http", config.host, "/account/authenticate/") should be (true)
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
	checkURI(request, "http", config.host, "/account/authenticate/") should be (true)
      }

    }

    "called without appropriate configuration" should {
      
      val request1 = empty_gen.authenticateUser()
      val request2 = empty_gen.authenticateUser("test", "pass")
      
      "return None with parameters" in { request2 should be (None) }
      "return None without parameters" in { request1 should be (None) }

    }

  }

  "createObject() on CallGenerator" when {

    "called" should {

      val request = default_gen.createObject().get
      
      "return a PUT request" in { (request method) should equal ("PUT") }
      "produce URI corresponding to to http://host/neo/" in { checkURI(request, "http", config.host, "/neo/") should be (true) }

    }

    "called without configuration" should {

      val request = empty_gen.createObject()
      "return None" in { request should be (None) }

    }

  }

  "updateObject() on CallGenerator" when {

    "called" should {

      val request = default_gen.updateObject("test").get
      
      "return a POST request" in { (request method) should equal ("POST") }
      "produce URI corresponding to http://host/neo/id/" in { checkURI(request, "http", config.host, "/neo/test/") should be (true) }

    }

    "called without configuration" should {

      val request = empty_gen.updateObject("test")
      "return None" in { request should be (None) }

    }

    "called with empty string as id" should {

      "throw an IllegalArgumentException" in {
	evaluating { val request = default_gen.updateObject("").get } should produce [IllegalArgumentException]
      }

    }

  }

  "getObject() on CallGenerator" when {

    val neo_id = "test"

    "called" should {

      val request = default_gen.getObject(neo_id).get

      "return a GET request" in { (request method) should equal ("GET") }
      "produce URI corresponding to http://host/neo/id/" in {
	checkURI(request, "http", config.host, "/neo/" + neo_id + "/") should be (true)
      }

    }

    "called with empty configuration" should {
      val request = empty_gen.getObject(neo_id)
      "return None" in { request should be (None) }
    }

    "called with empty string as id" should {
      "throw an IllegalArgumentException" in {
	evaluating { val request = default_gen.getObject("").get } should produce [IllegalArgumentException]
      }
   
    }
  
  }

  "getData() on CallGenerator" when {

    val neo_id = "test"

    "called without query specification" should {

      val request = default_gen.getData(neo_id).get

      "return a GET request" in { (request method) should equal ("GET") }
      "produce URI corresponding to http://host/neo/data/id/" in {
	checkURI(request, "http", config.host, "/neo/data/" + neo_id + "/") should be (true)
      }

    }

    "called with parameters" should {

      val request = default_gen.getData(neo_id, Map("tes t" -> "%ds 33")).get

      "return a GET request" in { (request method) should equal ("GET") }
      "add escaped query variables" in { checkURI(request, "http", config.host, "/neo/data/" + neo_id + "/", "tes+t=%ds+33") should be (true) }

    }

    "called with empty configuration" should {
      val request = empty_gen.getData(neo_id)
      "return None" in { request should be (None) }
    }

    "called with empty string as id" should {
      "throw an IllegalArgumentException" in {
	evaluating { val request = default_gen.getData("").get } should produce [IllegalArgumentException]
      }
      
    }
    
  }

}
