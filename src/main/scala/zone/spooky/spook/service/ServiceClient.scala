package zone.spooky.spook.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest}
import akka.stream.{ActorMaterializerSettings, ActorMaterializer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import scala.concurrent.{Await}
import scala.util.control.NonFatal

object ServiceClient {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("service-client")
    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

    Stream.from(1).foreach { i =>
      val response = Http().singleRequest(HttpRequest(uri = s"http://localhost:8001/put?key=$i&value=$i"))
          .map(res => ("1", res))
        .recoverWith {
          case _ =>
            Http().singleRequest(HttpRequest(uri = s"http://localhost:8003/put?key=$i&value=$i"))
              .map(res => ("2", res))
        }

      try {
        val r = Await.result(response, Duration.Inf)
        println(s"Got successful response from host: ${r._1}")
      } catch {
        case NonFatal(e) =>
          println("Error getting data!")
      }

      Thread.sleep(1000)
    }
  }

}