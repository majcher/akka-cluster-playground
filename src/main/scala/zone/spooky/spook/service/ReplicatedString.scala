package zone.spooky.spook.service

import akka.cluster.ddata._

@SerialVersionUID(1L)
final case class ReplicatedString(value: String) extends ReplicatedData with ReplicatedDataSerialization {
  type T = ReplicatedString
  override def merge(that: ReplicatedString): ReplicatedString = that
}

object ReplicatedStringKey {
  def create(id: String): Key[ReplicatedString] = ReplicatedStringKey(id)
}

@SerialVersionUID(1L)
final case class ReplicatedStringKey(_id: String) extends Key[ReplicatedString](_id) with ReplicatedDataSerialization

