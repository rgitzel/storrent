package rodney

import java.io.InputStream
import java.net.{InetAddress, URL}

import org.saunter.bencode.BencodeDecoder
import org.storrent.{Torrent, Tracker}

import scala.io.Codec
import scala.io.Source._


case class TrackerResponse(interval: Long, peers: List[PeerConfig])

object TrackerResponse{

  def apply(is: InputStream): TrackerResponse = {
    val s = fromInputStream(is)(Codec.ISO8859).mkString
    val m = BencodeDecoder.decode(s).get.asInstanceOf[Map[String, Any]]
    apply(m)
  }

  def apply(filename: String): TrackerResponse = apply(Tracker.torrentFromBencode(filename))

  def apply(resp: Map[String, Any]): TrackerResponse = {
    new TrackerResponse(
      getOrThrow[Long](resp, "interval"),
      extractPeers(getOrThrow[String](resp, "peers"))
    )
  }

  protected def getOrThrow[T](m: Map[String, Any], key: String): T =
    m.getOrElse(key, throw new RuntimeException(s"torrent response is missing '${key}'")).asInstanceOf[T]


  protected def extractPeers(s: String) =
    Torrent.peersToIp(s).map{ case(ipString, port) =>
      PeerConfig(InetAddress.getByName(ipString), port)
    }
}
