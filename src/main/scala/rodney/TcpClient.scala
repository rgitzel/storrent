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

// TODO: caller should be pulled from the SendData message and cached until the response is done... something for the FSM version
class TcpClient(remote: InetSocketAddress, isCompleteResponse: ByteString => Boolean, caller: Option[ActorRef]) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)
  val socket = IOManager(context.system).connect(remote)
  var buffer = akka.util.ByteString()

  // TODO: better use a FSM
  var done = false

  def receive = {
    case IO.Connected(_, address) =>
      log.info("Connected")

    case IO.Closed(_, cause) =>
      log.info(s"connection to ${socket} closed: ${cause}")
      socket.close

    case IO.Read(_, bytes) =>
      if(!done) {
        buffer = buffer ++ bytes
        log.info(s"read ${bytes.size} bytes")
        if(isCompleteResponse(bytes)) {
          log.info(s"done! got ${buffer.size} bytes total")
          log.debug(s"received: ${buffer.utf8String.take(400)}...${buffer.utf8String.takeRight(400)}")
          done = true
          caller.map(_ ! TcpClient.CompletedResponse(buffer))
        }
        else {
          log.debug("not done yet")
        }
      }

    case TcpClient.SendData(bytes) =>
      log.info("writing " + bytes.utf8String)
      socket.asWritable.write(bytes)

    case TcpClient.CloseConnection =>
      log.info("closing")
      socket.close
  }
}

