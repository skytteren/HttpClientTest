package httpClientTest

import scala.io.Source
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Failure
import akka.actor.PoisonPill
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import java.util.concurrent.TimeoutException

case class OK(url: String, timeSpent: Long)
case class Error(url: String, timeSpent: Long)

trait TestRig {
	
	val urls = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("test.urls.txt"), "UTF-8").getLines.map(_.toLowerCase).toVector
	
	def test(httpClient: HttpClient){
		
		val system = ActorSystem("HttpClientTest")
		
		def now = System.currentTimeMillis
		
		val done = Promise[(Int, Int, Long)]()
		val resultHandler = system.actorOf(Props(new ResultHandler(urls, done)))
		println("size: " + urls.size)
		val start = now
		var i = 0
		urls.foreach(url => {
			val queryStart = now
			import akka.pattern.after
			val timeout = after(60 seconds, system.scheduler)(Future.failed(new TimeoutException("Timeout")))
			
			val query = httpClient.query(url)
			(Future firstCompletedOf Seq(query, timeout)).onComplete(_ match {
				case Success(result) =>
					println(i + " Success: " + result._1 + " " + result._3 + " " + url)
					i += 1
					resultHandler ! OK(url, now - queryStart)
				case Failure(throwable) =>
					println(i + "Failure: " + url + " " + throwable.getMessage())
					i += 1
					resultHandler ! Error(url, now - queryStart)
			})
		})
		
		done.future.onComplete(_ match{
			case Success((numberOfOKs, numberOfErrors, totalTimeSpent)) => 
				println("Done")
				println("OK: " + numberOfOKs)
				println("Errors: " + numberOfErrors)
				println("Total time spent: " + totalTimeSpent)
				println("Time spent: " + (now - start))
				system.shutdown()
				httpClient.done()
				System.exit(0)
			case Failure(t) =>
				System.err.print("Problems")
				t.printStackTrace()
				system.shutdown()
				httpClient.done()
				System.exit(1)
		})
		
	}
	
}

class ResultHandler(urls: Vector[String], done:Promise[(Int, Int, Long)]) extends Actor{
	
	val numberOfHits = urls.size
	
	var doneUrls = Set[String]()
	
	var numberOfOKs = 0
	var numberOfErrors = 0
	var totalTimeSpent: Long = 0;
	var numberOfResultsReceived = 0
	
	context.system.scheduler.scheduleOnce(2 minutes)(self ! "Done")
	
	def receive = {
		case OK(url, timeSpent) => 
			numberOfOKs += 1
			doneUrls += url
			update(timeSpent)
		case Error(url, timeSpent) => 
			numberOfErrors += 1
			doneUrls += url
			update(timeSpent)
		case _ =>
			println("Done")
			(urls diff doneUrls.toSeq).foreach(println)
			done.success((numberOfOKs, numberOfErrors, this.totalTimeSpent))
			self ! PoisonPill
			context.system.shutdown()
	}
	
	def update(timeSpent: Long) {
		this.totalTimeSpent += timeSpent
		numberOfResultsReceived += 1
		if(numberOfHits <= numberOfResultsReceived){ 
			done.success((numberOfOKs, numberOfErrors, this.totalTimeSpent))
			self ! PoisonPill
		}
	}
	
}