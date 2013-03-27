package org.storrent

import akka.actor.{ Actor, ActorRef, ActorLogging, Props}
import org.saunter.bencode._
import scala.io.Source.{ fromInputStream }
import java.net.{ URLEncoder, URL }

object Tracker {
  case class PingTracker
}

class Tracker(torrentName: String) extends Actor with ActorLogging {
  import Tracker._

  def hexStringURLEncode(x: String) = { x.grouped(2).toList.map("%" + _).mkString("") }

  def receive = {
    case PingTracker =>
      val source = scala.io.Source.fromFile(torrentName, "macintosh")
      val metainfo = source.mkString
      source.close()
      val decodedMeta = BencodeDecoder.decode(metainfo)

      //this is a hack to get around type erasure warnings. It seems that the correct fix is to use the Manifest in the bencode library
      val metaMap = decodedMeta.get.asInstanceOf[Map[String, Any]]
      val infoMap = metaMap.get("info").get.asInstanceOf[Map[String, Any]]
      val fileLength = infoMap.get("length").get.asInstanceOf[Long]
      val pieceLength = infoMap.get("piece length").get.asInstanceOf[Long]
      val encodedInfoMap = BencodeEncoder.encode(infoMap)
      val numPieces = fileLength / pieceLength + (fileLength % pieceLength) % 1
      val md = java.security.MessageDigest.getInstance("SHA-1")
      val infoSHABytes = md.digest(encodedInfoMap.getBytes).map(0xFF & _)
      val infoSHA = infoSHABytes.map { "%02x".format(_) }.foldLeft("") { _ + _ } //taken from Play
      val infoSHAEncoded = hexStringURLEncode(infoSHA)
      val params = Map("port" -> "63211", "uploaded" -> "0", "downloaded" -> "0", "left" -> "1277987")
      val encodedParams = (for ((k, v) <- params) yield URLEncoder.encode(k) + "=" + URLEncoder.encode(v)).mkString("&")
      val infoSHAParam = s"info_hash=${infoSHAEncoded}"
      val peerIdParam = s"peer_id=${infoSHAEncoded}" //FIXME: peer id should obviously not be the same as our hash
      val allParams = s"?${infoSHAParam}&${peerIdParam}&${encodedParams}"
      val completeUrl = "http://thomasballinger.com:6969/announce" + allParams
      val url = new URL(completeUrl)
      val trackerResponse = fromInputStream(url.openStream, "macintosh").getLines.mkString("\n")
      val decodedTrackerResponse = BencodeDecoder.decode(trackerResponse)
      val someTrackerResponse = decodedTrackerResponse.get.asInstanceOf[Map[String, String]]
      val peers = someTrackerResponse.get("peers").get

      sender ! (peers, infoSHABytes, fileLength, pieceLength, numPieces)
  }
}