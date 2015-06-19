package rodney

import java.net.{DatagramPacket, DatagramSocket, InetAddress}

import scala.util.{Failure, Try}

class UdpClient(remoteAddress: String, port: Int) {

  private val remote = InetAddress.getByName(remoteAddress)

  def execute(message: String): Try[String] = {
    Try {
      val data = new DatagramPacket(message.getBytes, 0, message.length, remote, port)
      val socket = new DatagramSocket()
      try {
        socket.setSoTimeout(UdpClient.Timeout)
        socket.send(data)
        receive(socket)
      }
      finally {
        socket.close()
      }
    }
    .map { result =>
      println(logPrefix(message) + s"returned '${result}'")
      result
    }
    .recoverWith {
      case ex: Exception =>
        println(logPrefix(message) + s"failed: " + ex)
        Failure(ex)
    }
  }

  private def logPrefix(message: String) = s"UDP message '${message}' to '${remoteAddress}:${port}' "

  private def receive(socket: DatagramSocket): String = {
    val buffer = new Array[Byte](512)
    val receivedPacket = new DatagramPacket(buffer, buffer.length)
    socket.receive(receivedPacket)
    new String(receivedPacket.getData, 0, receivedPacket.getLength).trim
  }
}


object UdpClient {
  val Timeout = 250

  // the typical constructor: you'd only provide a different Clock for log testing purposes
  def apply(remoteAddress: String, port: Int) = new UdpClient(remoteAddress, port)
}
