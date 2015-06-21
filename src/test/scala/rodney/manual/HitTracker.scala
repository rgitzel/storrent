package rodney.manual

import java.net.URL

import org.saunter.bencode.BencodeDecoder
import org.storrent.Tracker
import rodney.{TrackerResponse, TorrentConfig}

import scala.io.Codec
import scala.io.Source._


object HitTracker extends App {

  // something is quite wrong with the SHA-1 calculation... I have to override it with the one from the site
  //  http://www.legittorrents.info/index.php?page=torrent-details&id=fc8a15a2faf2734dbb1dc5f7afdc5c9beaeb1f59
  //val t = TorrentConfig("src/test/resources/rodney/ubuntu-15.04-desktop-amd64.iso.torrent").copy(infoSha1 = "fc8a15a2faf2734dbb1dc5f7afdc5c9beaeb1f59")

  val t = TorrentConfig(new URL("http://releases.ubuntu.com/15.04/ubuntu-15.04-desktop-amd64.iso.torrent"))
  println(t.infoSha1)

  if(t.trackerUrl.startsWith("http")) {
    val trackerResponse = fromInputStream(new URL(t.trackerUrl).openStream)(Codec.ISO8859).mkString
    println("response from tracker: " + BencodeDecoder.decode(trackerResponse).get.asInstanceOf[Map[String, Any]])
  }
  else {
    println("only http torrents for now")
  }
}
