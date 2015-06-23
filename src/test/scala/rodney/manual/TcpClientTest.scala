package rodney.manual

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.util.ByteString
import rodney.TcpClient


object TcpClientTest extends App {
  val host = "www.sunet.se"

  val system = ActorSystem("tcp-test")
  val tcp = system.actorOf(Props(new TcpClient(new InetSocketAddress(host, 80))), "tcp")

  tcp ! TcpClient.SendData(ByteString("GET / HTTP/1.1\n"))
  tcp ! TcpClient.SendData(ByteString("host: " + host + "\n\n"))

  Thread.sleep(5500)
  tcp ! TcpClient.CloseConnection
  Thread.sleep(100)

  system.shutdown()

  println("done")
}
