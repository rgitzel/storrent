package rodney.manual

import java.net.URL

import akka.actor.{ActorSystem, Props}
import rodney._
import rodney.peer.PeerInteraction
import rodney.tracker.{TrackerConfig, TrackerResponse}

object SampleFlow extends App {

  val system = ActorSystem("rodney-torrent")

  val t = TrackerConfig(TestData.ubuntuUrl)
//  val t = TorrentConfig(TestData.ubuntuFilename)

  println(t.trackerUrl)

  if(t.trackerUrl.startsWith("http")) {

    val trackerResponse = TrackerResponse(new URL(t.trackerUrl).openStream)
    println("response from tracker: " + trackerResponse)

    for(i <- 0 until 1) {
      val peer = trackerResponse.peers(i)
      val dl = system.actorOf(Props(new PeerInteraction(peer)), "peer-" + i)
      dl ! PeerInteraction.Start(t.infoSha)
    }

    // TODO: sort out a reasonable shutdown...
    Thread.sleep(10000)

    system.shutdown()
  }
  else {
    println("only http torrents for now")
  }
}
