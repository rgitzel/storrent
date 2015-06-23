package rodney

import java.io.File
import java.net.InetAddress

import org.scalatest.{FlatSpec, Matchers}


class TrackerResponseSpec extends FlatSpec with Matchers {

  behavior of "apply"

  it should "work on ubuntu response" in {
    // 300 is the size of the list in the bencoded file, and it takes 6 bytes for IP and port
    val expectedPeers = 300 / 6

    val resp = TrackerResponse(new File("src/test/resources/rodney/ubuntu.announce.response"))

    resp.interval should be (1800)

    resp.peers.size should be (expectedPeers)

    resp.peers.head should be (PeerConfig(InetAddress.getByName("50.171.7.7"), 6888))
    resp.peers.last should be (PeerConfig(InetAddress.getByName("79.141.160.77"), 45156))
  }
}
