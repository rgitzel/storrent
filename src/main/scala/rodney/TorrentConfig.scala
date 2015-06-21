package rodney

import java.net.{URL, URLEncoder}

import org.saunter.bencode.{BencodeDecoder, BencodeEncoder}
import org.storrent.Tracker

import scala.io.Codec
import scala.io.Source._


case class TorrentConfig(announceUrl: String, fileLength: Long, pieceLength: Long, infoSha1: String) {

  // aha!  https://wiki.theory.org/BitTorrentSpecification#Tracker_Request_Parameters

  // we can set this to anything, so long as it's 20 characters...
  val PeerId = "RODNEYRODNEYRODNEYRO"

  val Port = 6882 // we're not listening yet so this really doesn't matter

  val trackerUrl = {
    val params = Map(
      "info_hash" -> Tracker.hexStringURLEncode(infoSha1),
      "peer_id"     -> PeerId,
      "port"        -> Port,
      "downloaded"  -> 0,
      "uploaded"    -> 0,
      "left"        -> fileLength,
      "compact"     -> 1            // http://stackoverflow.com/a/24001128/107444
    )

    // combine them in key order to make testing possible
    val encodedParams = params.toList.sortBy(_._1).map{case(k,v) => k + "=" + v}.mkString("&")
    announceUrl + "?" + encodedParams
  }

  val numPieces = fileLength / pieceLength + (fileLength % pieceLength) % 2 - 1  
}


object TorrentConfig {

  def apply(url: URL): TorrentConfig = {
    val trackerResponse = fromInputStream(url.openStream)(Codec.ISO8859).mkString
    val decodedMeta = BencodeDecoder.decode(trackerResponse)
    apply(decodedMeta.get.asInstanceOf[Map[String, Any]])
  }

  def apply(filename: String): TorrentConfig = apply(Tracker.torrentFromBencode(filename))

  def apply(meta: Map[String, Any]): TorrentConfig = {
    val info = meta.get("info").get.asInstanceOf[Map[String, Any]]
    new TorrentConfig(
      getOrThrow[String]( meta, "announce"),
      getOrThrow[Long](   info, "length"),
      getOrThrow[Long](   info, "piece length"),
      sha1(info)
    )
  }

  protected def getOrThrow[T](m: Map[String, Any], key: String): T =
    m.getOrElse(key, throw new RuntimeException(s"torrent is missing '${key}'")).asInstanceOf[T]


  def sha1(info: Map[String, Any]): String = {
    // from Tracker.assembleTrackerInfo
    val encodedInfoMap = BencodeEncoder.encode(info)
    sha1(encodedInfoMap)
  }

  def sha1(s: String): String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val infoSHABytes = md.digest(s.getBytes("ISO-8859-1")).map(0xFF & _)
    val x = infoSHABytes.map { "%02x".format(_) }.foldLeft("") { _ + _ } //taken from Play
    println("calculated sha1 = " + x)
    x
  }
}





