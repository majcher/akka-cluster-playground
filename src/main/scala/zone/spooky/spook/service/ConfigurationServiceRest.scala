package zone.spooky.spook.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._

case class ConfigurationServiceRest(configActor: ActorRef)(implicit system: ActorSystem) {

  implicit val askTimeout = Timeout(5.seconds)

  val route =
    path("get") {
      get {
        onSuccess((configActor ? GetConfiguration).mapTo[GetConfigurationResponse]) { c =>
          complete(c.config.mkString(", \n"))
        }
      }
    } ~
    path("put") {
      parameters("key", "value") { (key, value) =>
        get {
          onSuccess(configActor ? ConfigurationChanged(key, value)) { _ =>
            complete("ok")
          }
        }
      }
    }
}