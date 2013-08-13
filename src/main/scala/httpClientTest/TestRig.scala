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

case class OK(timeSpent: Long)
case class Error(timeSpent: Long)

trait TestRig {
	
	val urls = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("test.urls.txt"), "UTF-8").getLines.map(_.toLowerCase).toVector
	
	def test(httpClient: HttpClient){
		
		val system = ActorSystem("HttpClientTest")
		
		def now = System.currentTimeMillis
		
		val done = Promise[(Int, Int, Long)]()
		val resultHandler = system.actorOf(Props(new ResultHandler(urls.size, done)))
		val start = now
		urls.par.foreach(url => {
			val queryStart = now
			httpClient.query(url).onComplete(_ match {
				case Success(result) =>
					println("Success: " + url)
					resultHandler ! OK(now - queryStart)
				case Failure(throwable) =>
					println("Failure: " + url)
					resultHandler ! Error(now - queryStart)
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
			case Failure(t) =>
				System.err.print("Problems")
				t.printStackTrace()
				system.shutdown()
				System.exit(1)
				httpClient.done()
		})
		
	}
	
}

class ResultHandler(numberOfHits: Int, done:Promise[(Int, Int, Long)]) extends Actor{
	
	var numberOfOKs = 0
	var numberOfErrors = 0
	var totalTimeSpent: Long = 0;
	var numberOfResultsReceived = 0
	
	def receive = {
		case OK(timeSpent) => 
			numberOfOKs += 1
			update(timeSpent)
		case Error(timeSpent) => 
			numberOfErrors += 1
			update(timeSpent)
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