package httpClientTest

import scala.concurrent.Future
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.client.methods.HttpGet
import org.apache.http.concurrent.FutureCallback
import org.apache.http.HttpResponse
import scala.concurrent.Promise

class ApacheAsyncHttpClient extends HttpClient{

	val requestConfig = RequestConfig.custom()
            .setSocketTimeout(5000)
            .setMaxRedirects(5)
            .setRedirectsEnabled(true)
            .setStaleConnectionCheckEnabled(true)
            .setConnectTimeout(5000).build();
	def httpClient = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setUserAgent("Mozilla/5.0 IsItDown.no spider")
            .build();
	def query(url: String): Future[(Int, String, Long)] = {
		val httpClient = this.httpClient
		httpClient.start();
		val start = System.currentTimeMillis
		val responsePromise = Promise[(Int, String, Long)]()
		 httpClient.execute(new HttpGet(url), new FutureCallback[HttpResponse]() {

        def completed(response: HttpResponse) {
        	responsePromise.success((response.getStatusLine().getStatusCode(), 
        			io.Source.fromInputStream(response.getEntity().getContent()).getLines.mkString, System.currentTimeMillis - start))
        	httpClient.close()
        }

        def failed(ex: Exception) {
        	responsePromise.failure(ex)
        	httpClient.close()
        }

        def cancelled() {
        	responsePromise.failure(null)
        	httpClient.close()
        }

    });
		responsePromise.future
	}
	
	def done(){
		httpClient.close()
	}
	
}

object ApacheAsyncHttpClient extends App with TestRig{
	
	test(new ApacheAsyncHttpClient)
	
}