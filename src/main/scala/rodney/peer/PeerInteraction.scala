package rodney.peer

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.ByteString
import org.storrent.Frame
import rodney.{InfoSha, TcpClient}


object PeerInteraction {
  case class DownloadPiece(infoSha: InfoSha)
}


class PeerInteraction(peer: PeerConfig) extends Actor with ActorLogging {

  val tcp = context.system.actorOf(
    Props(new TcpClient(new InetSocketAddress(peer.host, peer.port), pieceComplete, Some(self))),
    s"tcp-${peer.host.getHostAddress}-${peer.port}"
  )

  var gotFirstResponse = false

  def receive = {
    case PeerInteraction.DownloadPiece(infoSha) =>
      tcp ! TcpClient.SendData(Frame.createHandshakeFrame(infoSha.bytes.toArray))

    case TcpClient.CompletedResponse(bytes) =>
      log.info("response bytes: " + bytes)

      if(!gotFirstResponse) {
        log.info("Sending Interested message")
        tcp ! TcpClient.SendData(Frame.createInterestedFrame())
      }
      else {
        Frame.parseFrame(bytes) match {
          case (_, Some(message)) =>
            log.info("got peer response: " + PeerResponse.determineResponse(message))
          case other =>
            log.error("got bad peer response: " + PeerResponse.determineResponse(bytes))
        }
        tcp ! TcpClient.CloseConnection
      }
  }

  def pieceComplete(bs: ByteString) = {
    val pf = Frame.parseFrame(bs)
    pf._1 > 0 && pf._2.isDefined
  }
}