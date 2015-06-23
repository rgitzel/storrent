package rodney.manual

import java.net.{InetSocketAddress, URL}

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.ByteString
import org.storrent.Frame
import rodney._

object HitTracker extends App {

  val system = ActorSystem("rodney-storrent")

  val t = TrackerConfig(TestData.ubuntuUrl)
//  val t = TorrentConfig(TestData.ubuntuFilename)

  println(t.trackerUrl)

  if(t.trackerUrl.startsWith("http")) {

    val trackerResponse = TrackerResponse(new URL(t.trackerUrl).openStream)
    println("response from tracker: " + trackerResponse)

    for(i <- 0 until 1) {
      val peer = trackerResponse.peers(i)
      val dl = system.actorOf(Props(new PieceDownloader(peer)), "peer-" + i)
      dl ! PieceDownloader.DownloadPiece(t.infoSha)
    }

    // TODO: sort out a reasonable shutdown...
    Thread.sleep(5000)

    system.shutdown()
  }
  else {
    println("only http torrents for now")
  }
}
