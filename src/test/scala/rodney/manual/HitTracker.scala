package rodney.manual

import java.net.URL

import org.saunter.bencode.BencodeDecoder
import org.storrent.Tracker
import rodney.{TestData, TrackerResponse, TorrentConfig}

import scala.io.Codec
import scala.io.Source._


object HitTracker extends App {

  val t = TorrentConfig(TestData.ubuntuUrl)
//  val t = TorrentConfig(TestData.ubuntuFilename)

  println(t.trackerUrl)

  if(t.trackerUrl.startsWith("http")) {
    val trackerResponse = fromInputStream(new URL(t.trackerUrl).openStream)(Codec.ISO8859).mkString
    println("response from tracker: " + BencodeDecoder.decode(trackerResponse).get.asInstanceOf[Map[String, Any]])
  }
  else {
    println("only http torrents for now")
  }
}
