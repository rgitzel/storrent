package rodney

import org.scalatest.{FlatSpec, Matchers}

class TrackerResponseSpec extends FlatSpec with Matchers {

  behavior of "apply from Bdecoded map"

  it should "work on 'tom.torrent'" in {

    val expected = TorrentConfig(
      "http://thomasballinger.com:6969/announce",
      1277987,
      16384,
      TestData2.tomTorrentInfoSha
    )

    TorrentConfig("tom.torrent") should be (expected)
  }
}
