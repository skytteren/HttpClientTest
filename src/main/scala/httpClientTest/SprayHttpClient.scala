package httpClientTest

import spray.http._
import spray.client.pipelining._
import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.util.Timeout



class SprayAsyncHttpClient extends HttpClient{

	import spray.http._
	import spray.client.pipelining._
	import scala.concurrent.duration._
	implicit val system = ActorSystem("SprayClient")
	implicit val futureTimeout: Timeout = 5.seconds
	import system.dispatcher // execution context for futures
	
	val pipeline: HttpRequest => Future[HttpResponse] = /*addHeader("UserAgent", "Mozilla/5.0 IsItDown.no") ~> */sendReceive
	
	def query(url: String): Future[(Int, String, Long)] = {
		val start = System.currentTimeMillis
		pipeline(Get(url)).map(response => {
			println("map")
			(response.status.intValue, response.entity.asString, System.currentTimeMillis - start)
		})
	}
	
	def done(){
		system.shutdown()
	}
	
}

object SprayAsyncHttpClient extends App with TestRig{
	
	test(new ApacheAsyncHttpClient)
	
}