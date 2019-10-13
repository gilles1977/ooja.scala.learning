package example

import akka.actor.{ Actor, ActorLogging, Props }

object  DeviceManager {
    final case class RequestTrackDevice(groupId: String, deviceId: String)
    case object DeviceRegistered
}

class DeviceManager() extends Actor with ActorLogging {
    override def receive: Receive = {
        case _ => None
    }
}