package rodney

import org.scalatest.{FlatSpec, Matchers}

class TrackerResponseSpec extends FlatSpec with Matchers {

  behavior of "apply from Bdecoded map"

  it should "work on 'tom.torrent'" in {

    val expected = TorrentConfig(
      "http://thomasballinger.com:6969/announce",
      1277987,
      16384,
      "e073e640c01bde650c411c0406babe1741a698d5"
    )

    TorrentConfig("tom.torrent") should be (expected)
  }
}
