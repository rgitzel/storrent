package rodney

import org.storrent.{Torrent, Tracker}


case class TrackerResponse(interval: Long, peers: List[(String,Int)])

object TrackerResponse{

  def apply(filename: String): TrackerResponse = apply(Tracker.torrentFromBencode(filename))

  def apply(resp: Map[String, Any]): TrackerResponse = {
    new TrackerResponse(
      getOrThrow[Long](resp, "interval"),
      Torrent.peersToIp(getOrThrow[String](resp, "peers"))
    )
  }

  protected def getOrThrow[T](m: Map[String, Any], key: String): T =
    m.getOrElse(key, throw new RuntimeException(s"torrent response is missing '${key}'")).asInstanceOf[T]
}
