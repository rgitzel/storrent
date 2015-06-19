package rodney

import java.net.URLEncoder

import org.saunter.bencode.BencodeEncoder
import org.storrent.Tracker


case class TorrentConfig(announceUrl: String, fileLength: Long, pieceLength: Long, infoSha1: String) {

  // this seems to not matter... so long as it's exactly 20 chars... how did storrent work?
  val PeerId = "ABCDABCDABCDABCDABCD"

  val Port = 6882 // still not sure where this comes form, but this works for the Ubuntu torrent

  val trackerUrl = {
    val params = Map(
      "peer_id"     -> PeerId,
      "port"        -> Port,
      "downloaded"  -> 0,
      "uploaded"    -> 0,
      "left"        -> fileLength,
      "compact"     -> 1            // http://stackoverflow.com/a/24001128/107444
    )
    val encodedParams = params.keys.toList.sorted.map(key => key + "=" + URLEncoder.encode(params.getOrElse(key,"").toString)).mkString("&")
    val infoSHAParam = s"info_hash=${Tracker.hexStringURLEncode(infoSha1)}"
    val allParams = s"?${infoSHAParam}&${encodedParams}"
    announceUrl + allParams
  }

  val numPieces = fileLength / pieceLength + (fileLength % pieceLength) % 2 - 1  
}


object TorrentConfig {

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


  protected def sha1(info: Map[String, Any]) = {
    // from Tracker.assembleTrackerInfo
    val encodedInfoMap = BencodeEncoder.encode(info)
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val infoSHABytes = md.digest(encodedInfoMap.getBytes).map(0xFF & _)
    val x = infoSHABytes.map { "%02x".format(_) }.foldLeft("") { _ + _ } //taken from Play
    println("calculated sha1 = " + x)
    x
  }
}





