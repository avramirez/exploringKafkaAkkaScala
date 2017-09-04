package com.kafkaflight

import akka.actor.{Actor, Props}

class KafkaConsumer extends Actor {
  def receive = {
    case _ =>
  }
}

object KafkaConsumer {
  def props : Props = Props(new KafkaConsumer)
}