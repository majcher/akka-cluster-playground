package zone.spooky.spook.service

import akka.cluster.ddata.{Replicator, ORSetKey, ORSet, DistributedData}
import akka.cluster.ddata.Replicator._
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}

import language.postfixOps
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import akka.cluster.MemberStatus
import com.typesafe.config.ConfigFactory
import akka.pattern.ask

class ConfigurationService extends Actor {

  val replicator = DistributedData(context.system).replicator
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  val config = mutable.Map[String, String]()

  def receive = {
    case GetConfiguration =>
      //sender() ! GetConfigurationResponse(config.toMap)
      val keyOrset = ORSetKey[String]("key_1")
      val value: Future[String] = (replicator ? Get[ORSet[String]](keyOrset, ReadLocal)).mapTo[Replicator.GetSuccess[String]].map(s => s.get(s.key))
      val s = sender()
      value.map(v => s ! GetConfigurationResponse(Map("key_1" -> v)))

    case ConfigurationChanged(key, value) =>
      println(s"---- config changed: $key=$value")
      replicator ! Update(ORSetKey[String](key), ORSet.empty[String], WriteLocal)(_ + value)

//    case BackendRegistration if !backends.contains(sender()) =>
//      println("---- Backend registered: " + sender())
//      context watch sender()
//      backends = backends :+ sender()

//    case state: CurrentClusterState =>
//      state.members.filter(_.status == MemberStatus.Up) foreach register

//    case MemberUp(m) =>
  }

//  def register(member: Member): Unit =
//    if (member.hasRole("frontend"))
//      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
//        BackendRegistration
}