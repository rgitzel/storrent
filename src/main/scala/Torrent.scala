package org.storrent

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }
import akka.util.ByteString
import akka.pattern.ask
import scala.concurrent.Await
import org.apache.commons.io.FileUtils.writeByteArrayToFile
import scala.collection.mutable

object Torrent {
  case class ReceivedPiece(index: Int, data: ByteString)
  case class PeerHas(index: Int)
  case class PeerHasBitfield(peerBitfieldSet: mutable.Set[Int])
  case class PeerPieceRequest(sendingActor: ActorRef)
  case class TorrentInfo(peers: String, infoSHABytes: Array[Int], fileLength: Long, pieceLength: Long, numPieces: Long)

  def peersToIp(allPeers: String) = {
    val peers = allPeers.getBytes("ISO-8859-1").grouped(6).toList.map(_.map(0xFF & _))
    println(s"found ${peers.size} peers in string of length ${allPeers.size}")
//    peers.zipWithIndex.foreach{case(x,n) => println(n + " " + x.mkString("."))}
    val ips = peers.map(x => x.slice(0, 4).mkString("."))
    val ports = peers.map { x => (x(4) << 8) + x(5) } //convert 2 bytes to an int
    ips zip ports
  }
}

class Torrent(torrentName: String) extends Actor with ActorLogging {
  import Torrent._

  val weHavePiece: mutable.Set[Int] = mutable.Set()
  // FIXME: it seems redunant to have peerSeen when we have peerHasPiece, but peerHasPiece takes an ActorRef, which requires spawning an Actor
  val peerHasPiece = mutable.Map.empty[ActorRef, mutable.Set[Int]]
  val peerSeen: mutable.Set[Tuple2[String, Int]] = mutable.Set()
  val tracker = context.actorOf(Props(new Tracker(torrentName, self)), s"Tracker-${torrentName}")

  val r = new scala.util.Random(0)
  var numPieces: Long = 0
  var fileContents: Array[ByteString] = Array()

  def receive = {
    case ReceivedPiece(index, data) =>
      if (fileContents.length == 0)
        fileContents = Array.fill(numPieces.toInt) { akka.util.ByteString("") }
      fileContents(index) = data
      weHavePiece += index
      if (weHavePiece.size >= numPieces) {
        val file = new java.io.File("flag.jpg")
        fileContents.foreach { s => writeByteArrayToFile(file, s.toArray, true) }
        context.system.shutdown() // FIXME: will need to change when we handle more than one torrent at once
      }
    case PeerHas(index) =>
      peerHasPiece(sender) += index
    case PeerHasBitfield(peerBitfieldSet) =>
      peerHasPiece(sender) = peerBitfieldSet
    case PeerPieceRequest(sendingActor) =>
      val missing = (peerHasPiece(sendingActor) -- weHavePiece).toIndexedSeq
      val requestResult = missing.size match {
        case 0 => None
        case _ => Some(missing(r.nextInt(missing.size)))
      }
      sender ! (requestResult)
    case TorrentInfo(peers, infoSHABytes, fileLength, pieceLength, numP) =>
      numPieces = numP
      val ipPorts = peersToIp(peers)
      (ipPorts.toSet -- peerSeen).foreach { p =>
        println(s"Connecting to ${p._1}:${p._2}")
        val peer = context.actorOf(Props(new PeerConnection(p._1, p._2, self, infoSHABytes, fileLength, pieceLength)), s"PeerConnection-${p._1}:${p._2}")
        peerHasPiece += (peer -> mutable.Set())
        peerSeen += p
      }
  }
}
