package rodney

import org.storrent.Tracker


case class TrackerResponse(interval: Int)

object TrackerResponse{

  def apply(filename: String): TrackerResponse = apply(Tracker.torrentFromBencode(filename))

  def apply(resp: Map[String, Any]): TrackerResponse = {
    new TrackerResponse(
      getOrThrow[Int](resp, "interval")
    )
  }

  protected def getOrThrow[T](m: Map[String, Any], key: String): T =
    m.getOrElse(key, throw new RuntimeException(s"torrent response is missing '${key}'")).asInstanceOf[T]

}
