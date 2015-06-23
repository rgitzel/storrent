package rodney

import java.net.InetSocketAddress

import scala.concurrent.duration._

import akka.actor._
import akka.util.{ByteString, Timeout}

object TcpClient {
  case class DataReceived(buffer: ByteString)
  case object ConnectionClosed
  case object CloseConnection
  case class SendData(bytes: ByteString)
}

// c.f. https://gist.github.com/jboner/4451490

class TcpClient(server: InetSocketAddress) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)
  val socket = IOManager(context.system).connect(server)
  var buffer: ByteString = akka.util.ByteString()

  def receive = {
    case IO.Connected(`socket`, address) =>
      log.error("Connected")

    case IO.Closed(`socket`, cause) =>
      log.error(s"connection to ${socket} closed: ${cause}")
      socket.close

    case IO.Read(`socket`, bytes) =>
      buffer = buffer ++ bytes
      log.error(s"read ${bytes.size} bytes")

    case TcpClient.SendData(bytes) =>
      log.error("writing " + bytes.utf8String)
      socket.asWritable.write(bytes)

    case TcpClient.CloseConnection =>
      log.error("closing")
      socket.close
  }
}




