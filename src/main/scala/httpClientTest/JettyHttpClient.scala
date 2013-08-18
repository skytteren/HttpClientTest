package httpClientTest

import scala.concurrent.Future
import scala.concurrent.Promise
import org.eclipse.jetty.client.{HttpClient => JettyHttpClient}
import java.util.concurrent.TimeUnit
import org.eclipse.jetty.client.api.Response
import org.eclipse.jetty.client.api.Result
import org.eclipse.jetty.http.HttpField
import java.nio.ByteBuffer
import java.nio.charset.Charset
import org.eclipse.jetty.util.ssl.SslContextFactory

class JettyAsyncHttpClient extends HttpClient{

	val sslContextFactory = new SslContextFactory();
	def client = {
		val client = new JettyHttpClient(sslContextFactory);
		client.setMaxConnectionsPerDestination(200); // max 200 concurrent connections to every address
		client.setIdleTimeout(10000); // 30 seconds timeout; if no server reply, the request expires
		client.setConnectTimeout(10000)
		client
	}
	def query(url: String): Future[(Int, String, Long)] = {
		val httpClient = this.client
		httpClient.start();
		val start = System.currentTimeMillis
		val responsePromise = Promise[(Int, String, Long)]()
		val request = httpClient.newRequest(url).
        timeout(10, TimeUnit.SECONDS).
        agent("Mozilla/5.0 IsItDown.no spider").
        onResponseFailure(new Response.FailureListener(){
        	override def onFailure(response: Response, failure: Throwable){
        		responsePromise.failure(failure)
        		httpClient.stop()
        	}
        }).
        send(new Response.Listener.Empty(){
          override def onContent(response: Response, buffer: ByteBuffer){
          	responsePromise.success((response.getStatus(), 
          			new String( buffer.array(), Charset.forName("UTF-8")), System.currentTimeMillis - start))
      			httpClient.stop()
          }
        });
		responsePromise.future
	}
	
	def done(){
		//client.stop()
	}
	
}

object JettyAsyncHttpClient extends App with TestRig{
	
	test(new JettyAsyncHttpClient)
	
}