package example

import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.ActorRef
import example.DeviceManager.RequestTrackDevice

object  DeviceGroup {
    def props(groupId: String): Props = Props(new DeviceGroup(groupId))
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
    var deviceIdToActor = Map.empty[String, ActorRef]

    override def preStart(): Unit = log.info("DeviceGroup {} started", groupId)

    override def postStop(): Unit = log.info("DeviceGroup {} stopped", groupId)

    override def receive: Actor.Receive = {
        case trackMsg @ RequestTrackDevice(`groupId`, _) =>
            deviceIdToActor.get(trackMsg.deviceId) match {
                case Some(deviceActor) => 
                    deviceActor.forward(trackMsg)
                case None => 
                    log.info("Creating device actor for {}", trackMsg.deviceId)
                    val deviceActor = context.actorOf(Device.props(groupId, trackMsg.deviceId), s"device-${trackMsg.deviceId}")
                    deviceIdToActor += trackMsg.deviceId -> deviceActor
                    deviceActor.forward(trackMsg)
            }
        
        case RequestTrackDevice(groupId, deviceId) =>
            log.warning("Ignoring TrackDevice request for {}. I'm responsible for {}.", groupId, this.groupId)
    }
}