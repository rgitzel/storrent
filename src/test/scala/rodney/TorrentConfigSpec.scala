package rodney

import org.saunter.bencode.{BencodeEncoder, BencodeDecoder}
import org.scalatest.{FlatSpec, Matchers}
import org.storrent.Tracker
import TestData2._


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
      tomTorrentInfoSha
    )
    TorrentConfig("tom.torrent") should be (expected)
  }


  it should "work on ubuntu" in {
    val expected = TorrentConfig(
      "http://torrent.ubuntu.com:6969/announce",
      1150844928,
      524288,
      ubuntuInfoSha
    )
    TorrentConfig(ubuntuFilename) should be (expected)
  }


  behavior of "sha1"

  it should "generate the right hash for the ubuntu torrent" in {
    val meta = Tracker.torrentFromBencode(ubuntuFilename)

    TorrentConfig.sha1(meta.get("info").get.asInstanceOf[Map[String,Any]]) should be (ubuntuInfoSha)
  }

  // this came from sorting out why the SHA1 was wrong, turned out to be not passing
  //  the encoding to .getBytes()... doesn't hurt to leave this here
  it should "pull out correct 'info' string" in {
    val source = scala.io.Source.fromFile(ubuntuFilename, "ISO-8859-1")
    val s = source.mkString
    source.close()

    val d = BencodeDecoder.decode(s).get.asInstanceOf[Map[String,Any]]
    d.keys should be (Set("announce", "creation date", "announce-list", "info", "comment"))
    val info = d.get("info").get.asInstanceOf[Map[String,Any]]
    info.keys should be ( Set("length", "name", "piece length", "pieces"))
    info.get("pieces").get.asInstanceOf[String].size should be (43920)

    val encodedInfo = BencodeEncoder.encode(info)
    s.contains(encodedInfo) should be (true)
  }
}
