package rodney

import org.scalatest.{FlatSpec, Matchers}
import org.storrent.Tracker

class TorrentConfigSpec extends FlatSpec with Matchers {

  behavior of "trackerUrl"

  it should "generate URL" in {
    // at this point, these are just values, they don't have to be sensible
    val conf = TorrentConfig("http://foo.com/announce", 99, 88, "abc123")

    val expected =
      "http://foo.com/announce?" +
        "compact=1&" +
        "downloaded=0&" +
        "info_hash=%ab%c1%23&" +
        "left=99&" +
        "peer_id=RODNEYRODNEYRODNEYRO&" +
        "port=6882&" +
        "uploaded=0"

    conf.trackerUrl should be (expected)
  }


  behavior of "apply from Bdecoded file"

  it should "work on 'tom.torrent'" in {
    val expected = TorrentConfig(
      "http://thomasballinger.com:6969/announce",
      1277987,
      16384,
      "e073e640c01bde650c411c0406babe1741a698d5"
    )
    TorrentConfig("tom.torrent") should be (expected)
  }


  it should "work on ubuntu" in {
    val expected = TorrentConfig(
      "http://torrent.ubuntu.com:6969/announce",
      1150844928,
      524288,
      "8de8404303b38385df58054ac9be5f914e91830e" // TODO: this is wrong!!
    )
    TorrentConfig("src/test/resources/rodney/ubuntu-15.04-desktop-amd64.iso.torrent") should be (expected)
  }


  behavior of "sha1"

  // TODO: what the is wrong, here... presumably something to do with the encoding...
  it should "generate the right hash for the ubuntu torrent" in {
    val meta = Tracker.torrentFromBencode("src/test/resources/rodney/ubuntu-15.04-desktop-amd64.iso.torrent")

    TorrentConfig.sha1(meta.get("info").get.asInstanceOf[Map[String,Any]]) should be ("fc8a15a2faf2734dbb1dc5f7afdc5c9beaeb1f59")
  }
}
