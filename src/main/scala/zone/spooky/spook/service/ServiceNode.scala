package zone.spooky.spook.service

import akka.actor.{ActorSystem, Props}
import akka.cluster.singleton.{ClusterSingletonManagerSettings, ClusterSingletonManager}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ServiceNode {

  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
      withFallback(ConfigFactory.load())

    implicit val system = ActorSystem("ClusterSystem", config)
    val configActor = system.actorOf(Props[ConfigurationService], name = "config")

    implicit val materializer = ActorMaterializer()
    val bindingFuture = Http().bindAndHandle(ConfigurationServiceRest(configActor).route, "localhost", port.toInt + 1)

    Await.ready(bindingFuture, Duration.Inf)
  }

}