package httpClientTest

import scala.concurrent.Future
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.AsyncHttpClient
import scala.concurrent.Promise
import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.Response

class NingAsyncHttpClient extends HttpClient {

	val config = new AsyncHttpClientConfig.Builder().
				setCompressionEnabled(true).
				setAllowPoolingConnection(true).
				setConnectionTimeoutInMs(10000).
				setRequestTimeoutInMs(10000).
				setFollowRedirects(true).
				setUserAgent("IsItDown.no").
				setMaxRequestRetry(2).
				build();
	val asyncHttpClient = new AsyncHttpClient(config)
	
	def query(url: String): Future[(Int, String, Long)] = {
		val start = System.currentTimeMillis
		val responsePromise = Promise[(Int, String, Long)]()
		asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler[Response](){

	    override def onCompleted(response: Response): Response = {
	    	responsePromise.success((response.getStatusCode, response.getResponseBody(), System.currentTimeMillis - start))
        response
	    }

	    override def onThrowable(t: Throwable){
	    	responsePromise.failure(t)
	    }
		});
		responsePromise.future
	}
	
	def done(){
		asyncHttpClient.close()
	}
	
}

object NingAsyncHttpClient extends App with TestRig{
	
	test(new NingAsyncHttpClient)
	
}