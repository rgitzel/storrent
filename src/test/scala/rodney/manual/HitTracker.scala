package rodney.manual

import java.net.{InetSocketAddress, URL}

import akka.actor.{Actor, ActorSystem, Props}
import org.storrent.Frame
import rodney.{TcpClient, TestData, TorrentConfig, TrackerResponse}

object HitTracker extends App {

  val system = ActorSystem("rodney-storrent")

  val t = TorrentConfig(TestData.ubuntuUrl)
//  val t = TorrentConfig(TestData.ubuntuFilename)

  println(t.trackerUrl)

  if(t.trackerUrl.startsWith("http")) {
    val trackerResponse = TrackerResponse(new URL(t.trackerUrl).openStream)
    println("response from tracker: " + trackerResponse)

    // can we hit a peer now?
    val peer = trackerResponse.peers(0)

    val tcp = system.actorOf(Props(new TcpClient(new InetSocketAddress(peer.host, peer.port))), "tcp")

    tcp ! TcpClient.SendData(Frame.createHandshakeFrame(t.infoSha.bytes.toArray))

    Thread.sleep(1500)

    tcp ! TcpClient.CloseConnection

    Thread.sleep(100)
    system.shutdown()
  }
  else {
    println("only http torrents for now")
  }
}
