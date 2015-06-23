package rodney

import java.net.InetAddress

// what we pull out of the tracker's response for a given peer... which isn't much, at the moment

case class PeerConfig(host: InetAddress, port: Int)
