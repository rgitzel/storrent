package rodney.peer

import akka.util.ByteString
import org.storrent.BTProtocol._
import org.storrent.Frame._

import scala.collection.mutable

sealed trait PeerResponse

case object ChokePeerResponse extends PeerResponse
case object UnchokePeerResponse extends PeerResponse
case class HavePeerResponse(index: Int) extends PeerResponse
case class BitFieldPeerResponse(set: Set[Int]) extends PeerResponse
case class PiecePeerResponse(index: Int, chunk: ByteString) extends PeerResponse

object PeerResponse {
  def determineResponse(m: ByteString): PeerResponse = {
    println("determineResponse: " + m)
    val rest = m.drop(1)
    m(0) & 0xFF match {
      case 0 => ChokePeerResponse
      case 1 => UnchokePeerResponse
      case 4 => HavePeerResponse(bytesToInt(rest.take(4)))
      case 5 =>
        var peerBitfieldSet: mutable.Set[Int] = mutable.Set()
        bitfieldToSet(rest, 0, peerBitfieldSet)
        BitFieldPeerResponse(peerBitfieldSet.toSet)
      case 7 =>
        val index = bytesToInt(rest.take(4))
        // FIXME: we assume that offset within piece is always 0
        PiecePeerResponse(index, rest.drop(4).drop(4))
      case other =>
        throw new RuntimeException("unrecognize peer response code: " + other)
    }
  }
}
