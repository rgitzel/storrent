package rodney

import java.net.URL


object TestData {
  // http://www.legittorrents.info/index.php?page=torrent-details&id=fc8a15a2faf2734dbb1dc5f7afdc5c9beaeb1f59
  val ubuntuFilename = "src/test/resources/rodney/ubuntu-15.04-desktop-amd64.iso.torrent"
  val ubuntuUrl = new URL("http://releases.ubuntu.com/15.04/ubuntu-15.04-desktop-amd64.iso.torrent")
  val ubuntuInfoSha = InfoSha(
    List(252, 138, 21, 162, 250, 242, 115, 77, 187, 29, 197, 247, 175, 220, 92, 155, 234, 235, 31, 89),
    "fc8a15a2faf2734dbb1dc5f7afdc5c9beaeb1f59"
  )

  val tomTorrentInfoSha = InfoSha(
    List(43, 21, 202, 43, 253, 72, 205, 215, 109, 57, 236, 85, 163, 171, 27, 138, 87, 24, 10, 9),
    "2b15ca2bfd48cdd76d39ec55a3ab1b8a57180a09"
  )
}
