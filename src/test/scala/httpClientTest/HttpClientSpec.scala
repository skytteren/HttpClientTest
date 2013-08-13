package isitdown.service

import scala.concurrent.Await._
import scala.concurrent.duration._
import org.scalatest.FunSpec
import scala.collection.JavaConversions._
import httpClientTest.ApacheAsyncHttpClient
import httpClientTest.NingAsyncHttpClient

class HttpClientSpec extends FunSpec {

	describe("User Agent"){
		val httpClient = new ApacheAsyncHttpClient
		//val url = "https://kundeportal.opf.no/kontrollstasjon"
		val url = "https://webmail.bekk.no"
				
		it("should save result"){
			val (status, response,time) = result(httpClient.query(url), 10 seconds)
			println("body: " + response)
			println("time: " + time)
			assert(status === 200)
			
		}
	}
	
}