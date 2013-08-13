package httpClientTest

import scala.concurrent.Future

trait HttpClient {
	
	def query(url: String): Future[(Int, String, Long)]
	
	def done()
}