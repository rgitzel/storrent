
import org.saunter.bencode.{BencodeEncoder, BencodeDecoder}
import org.scalatest.{Matchers, FlatSpec}


case class EncodingTestPair(label: String, value: Any, encoding: String)


class BencodeSpec extends FlatSpec with Matchers {

  // this set of tests should work on any implementation of encoding/decoding, just
  //  update these two functions
  def decode(s: String): Option[Any] = BencodeDecoder.decode(s)
  def encode(x: Any): String = BencodeEncoder.encode(x)


  // define some valid data with associated encodings

  val ValidInteger = EncodingTestPair("positive integer", 987, "i987e")

  val ValidString = EncodingTestPair("simple string", "mango", "5:mango")

  val EncodedList = EncodingTestPair("list", List(ValidString.value, ValidInteger.value), "l" + ValidString.encoding + ValidInteger.encoding + "e")

  // note the sort order in the encoded string
  val EncodedDictionary = EncodingTestPair(
    "dictionary",
    Map("foo" -> ValidString.value, "bar" -> ValidInteger.value, "monkeys" -> EncodedList.value),
    "d" + "3:bar" + ValidInteger.encoding + "3:foo" + ValidString.encoding + "7:monkeys" + EncodedList.encoding + "e"
  )

  // by putting these in a list we don't have explicitly write a test for each of them
  val TestPairs = List(
    ValidInteger,
    EncodingTestPair("negative integer", -28367,       "i-28367e"),
    EncodingTestPair("long integer",     1150844928,   "i1150844928e"),
    ValidString,
    EncodedList,
    EncodedDictionary
  )


  val RealFileContents = {
    val source = scala.io.Source.fromFile("src/test/resources/rodney/ubuntu-15.04-desktop-amd64.iso.torrent", "ISO-8859-1")
    val s = source.mkString
    source.close()
    s
  }



  behavior of "decode"

  TestPairs.foreach{ pair =>
    it should s"decode: ${pair.label}" in {
      decode(pair.encoding) should be (Some(pair.value))
    }
  }


  it should "decode a real file" in {
    val d = decode(RealFileContents).get.asInstanceOf[Map[String,Any]]
    d.keys should be (Set("announce", "creation date", "announce-list", "info", "comment"))
    val info = d.get("info").get.asInstanceOf[Map[String,Any]]
    info.keys should be ( Set("length", "name", "piece length", "pieces"))
    info.get("pieces").get.asInstanceOf[String].size should be (43920)
  }



  behavior of "encode"

  TestPairs.foreach{ pair =>
    it should s"encode: ${pair.label}" in {
      encode(pair.value) should be (pair.encoding)
    }
  }


  behavior of "round-trip"

  def roundTrip(s: String): String = decode(s).map(encode(_)).get


  TestPairs.foreach{ pair =>
    it should s"round-trip: ${pair.label}" in {
      roundTrip(pair.encoding) should be (pair.encoding)
    }
  }


  it should "work on a real file" in {
    roundTrip(RealFileContents) should be (RealFileContents)
  }
}
