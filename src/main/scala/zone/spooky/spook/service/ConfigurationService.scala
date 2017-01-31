package zone.spooky.spook.service

import akka.actor.{Actor, Terminated}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success

class ConfigurationService extends Actor {

  implicit val cluster = Cluster(context.system)
  val distributedData = DistributedData(context.system)
  var replicator = context watch distributedData.replicator

  val configsMapKey = ORMultiMapKey.create[String]("configs")

  implicit val timeout = Timeout(5.seconds)

  def receive = {
    case GetConfiguration =>
      val senderHandle = sender()
      (replicator ? Get[ORMultiMap[String]](configsMapKey, ReadLocal))
        .mapTo[Replicator.GetResponse[ORMultiMap[String]]] onComplete {
        case Success(s: GetSuccess[ORMultiMap[String]]) =>
          val configs = s.get(s.key)
          senderHandle ! GetConfigurationSuccess(configs.entries.map(kv => (kv._1, kv._2.head)))
        case Success(NotFound(_, _)) =>
          senderHandle ! GetConfigurationSuccess(Map.empty[String, String])
        case f =>
          println("GetConfiguration failed: " + f)
          senderHandle ! GetConfigurationFailure
      }

    case ConfigurationChanged(key, value) =>
      val senderHandle = sender()
      (replicator ? Update(configsMapKey, ORMultiMap.empty[String], WriteLocal) { configs =>
        configs - key
        configs + ((key, Set(value)))
      }).mapTo[UpdateResponse[ORMultiMap[String]]] onComplete {
        case Success(UpdateSuccess(_, _)) => senderHandle ! ConfigurationChangedSuccess
        case f => senderHandle ! ConfigurationChangedFailure
      }

    case Terminated(actor) =>
      println("Local configuration storage might got corrupted. Shutting down.")
      context.system.terminate()
  }
}