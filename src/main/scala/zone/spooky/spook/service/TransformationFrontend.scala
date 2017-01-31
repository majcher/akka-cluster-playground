package zone.spooky.spook.service

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps

class TransformationFrontend extends Actor {

  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self,
    classOf[MemberUp],
    classOf[MemberWeaklyUp],
    classOf[MemberJoined],
    classOf[MemberLeft],
    classOf[MemberRemoved],
    classOf[LeaderChanged],
    classOf[RoleLeaderChanged],
    classOf[ReachableMember],
    classOf[UnreachableMember]
  )
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
//    case job: ConfigurationChanged if backends.isEmpty =>
//      println("---- No backend!")
//      sender() ! JobFailed("Service unavailable, try again later", job)

//    case job: ConfigurationChanged =>
//      println("---- Job scheduled: " + job.text + ". Backends: " + backends)
//      jobCounter += 1
//      backends(jobCounter % backends.size) forward job

//    case BackendRegistration if !backends.contains(sender()) =>
//      println("---- Backend registered: " + sender())
//      context watch sender()
//      backends = backends :+ sender()

    case MemberUp(member) =>
      println("---- MemberUp: " + member)
    case MemberWeaklyUp(member) =>
      println("---- MemberWeaklyUp: " + member)
    case MemberJoined(member) =>
      println("---- MemberJoined: " + member)
    case MemberLeft(member) =>
      println("---- MemberLeft: " + member)
    case MemberRemoved(member, previousStatus) =>
      println("---- MemberRemoved: " + member + ", previousStatus: " + previousStatus)
    case LeaderChanged(member) =>
      println("---- ReachableMember: " + member)
    case ReachableMember(member) =>
      println("---- ReachableMember: " + member)
    case UnreachableMember(member) =>
      println("---- UnreachableMember: " + member)
      backends = backends.filter(_.path.address != member.address)

    case Terminated(a) =>
      println("---- Actor terminated: " + a)
      backends = backends.filterNot(_ == a)
  }
}

object TransformationFrontend {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[TransformationFrontend], name = "frontend")
  }
}