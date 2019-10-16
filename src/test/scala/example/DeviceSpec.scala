package example

import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps

import Device._

class DeviceSpec(_system: ActorSystem)
    extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
{
    def this() = this(ActorSystem("DeviceSpec"))

    override def afterAll: Unit = {
        shutdown(system)
    }

    "Device actor" must {
        //#device-read-test
        "reply with empty reading if no temperature is known" in {
            val probe = TestProbe()
            val deviceActor = system.actorOf(Device.props("group", "device"))

            deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
            val response = probe.expectMsgType[Device.RespondTemperature]
            response.requestId should ===(42L)
            response.value should ===(None)
        }
        //#device-read-test

        "reply with recorded temperature if known" in {
            val probe = TestProbe()
            val deviceActor = system.actorOf(Device.props("group", "device"))

            deviceActor.tell(Device.RecordTemperature(requestId = 1, 18.5), probe.ref)
            probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
            
            deviceActor.tell(Device.ReadTemperature(requestId = 2), probe.ref)
            val response = probe.expectMsgType[Device.RespondTemperature]
            response.requestId should === (2L)
            response.value should === (Some(18.5F))
        }
    //#device-write-read-test
    // "reply with latest temperature reading" in {
    //   val probe = TestProbe()
    //   val deviceActor = system.actorOf(Device.props("group", "device"))

    //   deviceActor.tell(Device.RecordTemperature(requestId = 1, 24.0), probe.ref)
    //   probe.expectMsg(Device.TemperatureRecorded(requestId = 1))

    //   deviceActor.tell(Device.ReadTemperature(requestId = 2), probe.ref)
    //   val response1 = probe.expectMsgType[Device.RespondTemperature]
    //   response1.requestId should ===(2L)
    //   response1.value should ===(Some(24.0))

    //   deviceActor.tell(Device.RecordTemperature(requestId = 3, 55.0), probe.ref)
    //   probe.expectMsg(Device.TemperatureRecorded(requestId = 3))

    //   deviceActor.tell(Device.ReadTemperature(requestId = 4), probe.ref)
    //   val response2 = probe.expectMsgType[Device.RespondTemperature]
    //   response2.requestId should ===(4L)
    //   response2.value should ===(Some(55.0))
    // }
    //#device-write-read-test

        "reply to registration requests" in {
            val probe = TestProbe()
            val deviceActor = system.actorOf(Device.props("group", "device"))

            deviceActor.tell(DeviceManager.RequestTrackDevice("group", "device"), probe.ref)
            probe.expectMsg(DeviceManager.DeviceRegistered)
            probe.lastSender should === (deviceActor)
        }

        "ignore wrong registration requests" in {
            val probe = TestProbe()
            val deviceActor = system.actorOf(Device.props("group", "device"))

            deviceActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.ref)
            probe.expectNoMessage(500.millisecond)

            deviceActor.tell(DeviceManager.RequestTrackDevice("group", "wrongDevice"), probe.ref)
            probe.expectNoMessage(500.millisecond)
        }

        "be able to register a device actor" in {
            val probe = TestProbe()
            val groupActor = system.actorOf(DeviceGroup.props("group"))

            groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
            probe.expectMsg(DeviceManager.DeviceRegistered)
            val deviceActor1 = probe.lastSender

            groupActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
            probe.expectMsg(DeviceManager.DeviceRegistered)
            val deviceActor2 = probe.lastSender
            deviceActor1 should !==(deviceActor2)

            deviceActor1.tell(Device.RecordTemperature(1, 18.3), probe.ref)
            probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
            deviceActor2.tell(Device.RecordTemperature(2, 20.0), probe.ref)
            probe.expectMsg(Device.TemperatureRecorded(requestId = 2))
        }

        "ignore requests for wrong group id" in {
            val probe = TestProbe()
            val groupActor = system.actorOf(DeviceGroup.props("group"))

            groupActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.ref)
            probe.expectNoMessage(500.millisecond)
        }
    }
}