package rodney.tracker

import java.io.File
import java.net.URL

import org.saunter.bencode.BencodeDecoder
import org.storrent.Tracker
import rodney.InfoSha

import scala.io.Codec
import scala.io.Source._


case class TrackerConfig(announceUrl: String, fileLength: Long, pieceLength: Long, infoSha: InfoSha) {

  // https://wiki.theory.org/BitTorrentSpecification#Tracker_Request_Parameters

  // we can set this to anything, so long as it's 20 characters...
  val PeerId = "RODNEYRODNEYRODNEYRO"

  val Port = 6882 // we're not listening yet so this really doesn't matter

  val trackerUrl = {
    val params = Map(
      "info_hash" -> Tracker.hexStringURLEncode(infoSha.hexString),
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


object TrackerConfig {

  def apply(url: URL): TrackerConfig = {
    val trackerResponse = fromInputStream(url.openStream)(Codec.ISO8859).mkString
    val decodedMeta = BencodeDecoder.decode(trackerResponse)
    apply(decodedMeta.get.asInstanceOf[Map[String, Any]])
  }

  def apply(file: File): TrackerConfig = apply(Tracker.torrentFromBencode(file.getAbsolutePath))

  def apply(meta: Map[String, Any]): TrackerConfig = {
    val info = meta.get("info").get.asInstanceOf[Map[String, Any]]
    new TrackerConfig(
      getOrThrow[String]( meta, "announce"),
      getOrThrow[Long](   info, "length"),
      getOrThrow[Long](   info, "piece length"),
      InfoSha(info)
    )
  }

  protected def getOrThrow[T](m: Map[String, Any], key: String): T =
    m.getOrElse(key, throw new RuntimeException(s"torrent is missing '${key}'")).asInstanceOf[T]
}





