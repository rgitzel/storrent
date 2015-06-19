
import org.saunter.bencode.{BencodeEncoder, BencodeDecoder}
import org.scalatest.{Matchers, FlatSpec}


case class ValidEncoding(value: Any, encoding: String)


class BencodeSpec extends FlatSpec with Matchers {

  // this set of tests should work on any implementation of encoding/decoding, just
  //  update these two functions
  def decode(s: String): Option[Any] = BencodeDecoder.decode(s)
  def encode(x: Any): String = BencodeEncoder.encode(x)


  // define some valid data with associated encodings

  val ValidInteger          = ValidEncoding(987, "i987e")
  val ValidNegativeInteger  = ValidEncoding(-28367, "i-28367e")

  val EncodedString = "5:mango"
  val StringValue = "mango"

  val EncodedList = "l" + EncodedString + ValidInteger.encoding + "e"
  val ListValue = List(StringValue, ValidInteger.value)

  // note the sort order in the encoded string
  val EncodedDictionary = "d" + "3:bar" + ValidInteger.encoding + "3:foo" + EncodedString + "7:monkeys" + EncodedList + "e"
  val DictionaryValue = Map("foo" -> StringValue, "bar" -> ValidInteger.value, "monkeys" -> ListValue)



  behavior of "decode"

  it should "decode an integer" in {
    decode(ValidInteger.encoding) should be (Some(ValidInteger.value))
  }

  it should "decode a negative integer" in {
    decode(ValidNegativeInteger.encoding) should be (Some(ValidNegativeInteger.value))
  }

  it should "decode a string" in {
    decode(EncodedString) should be (Some(StringValue))
  }

  it should "decode a simple list" in {
    decode(EncodedList) should be (Some(ListValue))
  }

  it should "decode a simple dictionary" in {
    decode(EncodedDictionary) should be (Some(DictionaryValue))
  }



  behavior of "encode"

  it should "encode an integer" in {
    encode(ValidInteger.value) should be (ValidInteger.encoding)
  }

  it should "encode a negative integer" in {
    encode(ValidNegativeInteger.value) should be (ValidNegativeInteger.encoding)
  }

  it should "encode a string" in {
    encode(StringValue) should be (EncodedString)
  }

  it should "encode a simple list" in {
    encode(ListValue) should be (EncodedList)
  }

  it should "encode a simple dictionary" in {
    encode(DictionaryValue) should be (EncodedDictionary)
  }


  behavior of "round-trip"

  def roundTrip(s: String): String = decode(s).map(encode(_)).get

  it should "work on an integer" in {
    roundTrip(ValidInteger.encoding) should be (ValidInteger.encoding)
  }

  it should "work on an string" in {
    roundTrip(EncodedString) should be (EncodedString)
  }

  it should "work on a simple list" in {
    roundTrip(EncodedList) should be (EncodedList)
  }

  it should "work on a dictionary" in {
    roundTrip(EncodedDictionary) should be (EncodedDictionary)
  }

  it should "work on a real file" in {
    val source = scala.io.Source.fromFile("src/test/resources/rodney/ubuntu-15.04-desktop-amd64.iso.torrent", "ISO-8859-1")
    val s = source.mkString
    source.close()
    roundTrip(s) should be (s)
  }
}
