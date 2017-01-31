package zone.spooky.spook.service

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ConfigurationService extends Actor {

  implicit val cluster = Cluster(context.system)
  val replicator = DistributedData(context.system).replicator

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  implicit val timeout = Timeout(5.seconds)
  val configsMapKey = ORMultiMapKey.create[String]("configs")

  def receive = {
    case GetConfiguration =>
      val senderHandle = sender()
      (replicator ? Get[ORMultiMap[String]](configsMapKey, ReadLocal))
        .mapTo[Replicator.GetSuccess[ORMultiMap[String]]].map(s => s.get(s.key))
        .map(configs => senderHandle ! GetConfigurationResponse(configs.entries.map(kv => (kv._1, kv._2.head))))

    case ConfigurationChanged(key, value) =>
      replicator ! Update(configsMapKey, ORMultiMap.empty[String], WriteAll(5.seconds)) { configs =>
        configs - key
        configs + ((key, Set(value)))
      }
      sender() ! ConfigurationSaved
  }

}