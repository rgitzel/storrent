package rodney

import java.net.InetSocketAddress

import scala.concurrent.duration._

import akka.actor._
import akka.util.{ByteString, Timeout}

object TcpClient {
  case object CloseConnection
  case class SendData(bytes: ByteString)
  case class CompletedResponse(buffer: ByteString)
}

// c.f. https://gist.github.com/jboner/4451490

class TcpClient(server: InetSocketAddress, isComplete: ByteString => Boolean, caller: Option[ActorRef]) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)
  val socket = IOManager(context.system).connect(server)
  var buffer = akka.util.ByteString()

  // TODO: better use a FSM
  var done = false

  def receive = {
    case IO.Connected(_, address) =>
      log.error("Connected")

    case IO.Closed(_, cause) =>
      log.error(s"connection to ${socket} closed: ${cause}")
      socket.close

    case IO.Read(_, bytes) =>
      if(!done) {
        buffer = buffer ++ bytes
        log.error(s"read ${bytes.size} bytes")
        if(isComplete(bytes)) {
          log.error(s"done! got ${buffer.size} bytes total")
          done = true
          log.error(sender.toString())
          caller.map(_ ! TcpClient.CompletedResponse(buffer))
        }
        else {
          //        log.error("not done yet")
        }
      }

    case TcpClient.SendData(bytes) =>
      log.error("writing " + bytes.utf8String)
      socket.asWritable.write(bytes)

    case TcpClient.CloseConnection =>
      log.error("closing")
//      log.error(s"received: ${buffer.utf8String.take(400)}...${buffer.utf8String.takeRight(400)}")
      socket.close
  }
}




