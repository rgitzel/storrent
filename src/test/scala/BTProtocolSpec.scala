package org.storrent

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.matchers.MustMatchers

class BTProtocolSpec extends TestKit(ActorSystem("BTProtocolSpec"))
with ImplicitSender
with WordSpecLike
with MustMatchers
with BeforeAndAfterAll {
  import org.storrent.BTProtocol._
  import org.storrent.Frame._

  object fakeTCPClient {
  }

  trait fakeTCPClient extends TCPClientProvider {
    def recieve = Actor.emptyBehavior
  }

  val fakePeerConnect = TestProbe()

  def slicedBTProtocol = new BTProtocol("", 0, fakePeerConnect.ref ,Array.fill(20){0}, 16384*10, 16384) with fakeTCPClient

  // FIXME: this code is here because the client only supports recieving, for now
  def createChokeFrame(): ByteString = {
    val headerLenB = intToByte(1, 4)
    val headerIdB = ByteString(0)
    headerLenB ++ headerIdB
  }

  def createUnchokeFrame(): ByteString = {
    val headerLenB = intToByte(1, 4)
    val headerIdB = ByteString(1)
    headerLenB ++ headerIdB
  }

  def createHaveFrame(piece: Int): ByteString = {
    val headerLenB = intToByte(5, 4)
    val headerIdB = ByteString(4)
    val pieceB = intToByte(piece, 4)

    headerLenB ++ headerIdB ++ pieceB
  }

  def createHandshakeFrame(): ByteString = {
    ByteString(Array.fill(68){0.toByte})
  }


  "BTProtocol" should {
    "choke" in {
      val a = TestActorRef[BTProtocol](Props(slicedBTProtocol))
      a ! TCPClient.DataReceived(createHandshakeFrame())
      a ! TCPClient.DataReceived(createChokeFrame())
      fakePeerConnect.expectMsg(Choke())
    }
// these two fail under windows only?
//    "unchoke" in {
//      val a = TestActorRef[BTProtocol](Props(slicedBTProtocol))
//      a ! TCPClient.DataReceived(createHandshakeFrame())
//      a ! TCPClient.DataReceived(createUnchokeFrame())
//      fakePeerConnect.expectMsg(Unchoke())
//    }
//    "have" in {
//      val a = TestActorRef[BTProtocol](Props(slicedBTProtocol))
//      a ! TCPClient.DataReceived(createHandshakeFrame())
//      a ! TCPClient.DataReceived(createHaveFrame(1))
//      fakePeerConnect.expectMsg(Have(1))
//    }
  }
}



