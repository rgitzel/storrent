package rodney.peer

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.ByteString
import org.storrent.Frame
import rodney.{InfoSha, TcpClient}


object PeerInteraction {
  case class Start(infoSha: InfoSha)
}


class PeerInteraction(peer: PeerConfig) extends Actor with ActorLogging {

  val tcp = context.system.actorOf(
    Props(new TcpClient(new InetSocketAddress(peer.host, peer.port), pieceComplete, Some(self))),
    s"tcp-${peer.host.getHostAddress}-${peer.port}"
  )

  // TODO: definitely need FSM!
  var handshakeCompleted = false

  def receive = {
    case PeerInteraction.Start(infoSha) =>
      tcp ! TcpClient.SendData(Frame.createHandshakeFrame(infoSha.bytes.toArray))

    case TcpClient.CompletedResponse(bytes) =>
      log.info("response bytes: " + bytes)

      if(!handshakeCompleted) {
        log.info("Sending Interested message")
        tcp ! TcpClient.SendData(Frame.createInterestedFrame())
        handshakeCompleted = true
      }
      else {
        parseResponse(bytes) match {
          case Some(UnchokePeerResponse) =>
            println("they've unchoked us! :-)")
          case Some(BitFieldPeerResponse(set)) =>
            println(s"they've sent us a bitfield with ${set.size} items")
          case _ =>
            tcp ! TcpClient.CloseConnection
        }
      }
  }

  def parseResponse(bytes: ByteString): Option[PeerResponse] =
    Frame.parseFrame(bytes) match {
      case (_, Some(message)) =>
        val resp = PeerResponse.determineResponse(message)
        log.info("got peer response: " + resp)
        Some(resp)
      case other =>
        log.error("got bad peer response: " + PeerResponse.determineResponse(bytes))
        None
    }

  def pieceComplete(bs: ByteString) = {
    val pf = Frame.parseFrame(bs)
    pf._1 > 0 && pf._2.isDefined
  }
}
