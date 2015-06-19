package org.storrent

import akka.actor.{ Actor, ActorRef, Props, ActorSystem }

object Storrent {
  def main(args: Array[String]) {
    if(args.isEmpty) {
      println("Usage: Storrent [torrent file] [torrent file] ...")
    } else {
      val system = ActorSystem("storrent")
      args.foreach { f => system.actorOf(Props(new Torrent(f)), s"Torrent${f}") }
    }
  }
}
