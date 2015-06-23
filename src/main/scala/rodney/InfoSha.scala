package rodney

import org.saunter.bencode.BencodeEncoder


/*
 * looks like we want to keep both versions around, one string and one array of bytes;
 *  we need the former for the tracker URL, and the latter for peers...
 */
case class InfoSha(bytes: List[Int], hexString: String)

object InfoSha {
  def apply(info: Map[String, Any]): InfoSha = apply(BencodeEncoder.encode(info))

  def apply(s: String): InfoSha = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val infoSHABytes = md.digest(s.getBytes("ISO-8859-1")).map(0xFF & _)
    val hex = infoSHABytes.map { "%02x".format(_) }.foldLeft("") { _ + _ } //taken from Play
    //    println("calculated sha1 = " + x)
    new InfoSha(infoSHABytes.toList, hex)
  }
}


