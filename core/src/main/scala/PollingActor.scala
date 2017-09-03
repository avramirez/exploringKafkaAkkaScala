package com.kafkaflight

import akka.actor.{Actor, ActorLogging, Props}

class PollingActor extends Actor with ActorLogging {

  import PollingActor._

  def receive = {
    case GetRecentFlights => log.debug("Poll baby poll")
    case _ => log.error("Unknown Poll Operation")
  }
}

object PollingActor {
  def props = Props(new PollingActor())

  case object GetRecentFlights
}