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
    val trackerResponse = TrackerResponse(new URL(t.trackerUrl).openStream)
    println("response from tracker: " + trackerResponse)
  }
  else {
    println("only http torrents for now")
  }
}
