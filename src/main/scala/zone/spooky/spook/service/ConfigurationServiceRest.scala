package zone.spooky.spook.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import spray.json._

import scala.concurrent.duration._

case class ConfigurationServiceRest(configActor: ActorRef)
                                   (implicit system: ActorSystem)
  extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val askTimeout = Timeout(1.minute)

  val route =
    path("get") {
      get {
        onSuccess(configActor ? GetConfiguration) {
          case GetConfigurationSuccess(config) => complete(config)
          case GetConfigurationFailure => complete(StatusCodes.InternalServerError)
        }
      }
    } ~
    path("put") {
      parameters("key", "value") { (key, value) =>
        get {
          onSuccess(configActor ? ConfigurationChanged(key, value)) {
            case ConfigurationChangedSuccess => complete(StatusCodes.OK)
            case ConfigurationChangedFailure => complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
}