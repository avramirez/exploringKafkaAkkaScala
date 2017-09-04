package com.kafkaflight

import akka.actor.{Actor, ActorLogging, Props}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.kafkaflight.DomainObjects.Flight
import com.kafkaflight.KafkaConsumer.START_CONSUMER

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KafkaConsumer(consumerSettings : ConsumerSettings[Array[Byte], Array[Byte]])(implicit val mat: ActorMaterializer) extends Actor with ActorLogging{
  def receive = {
    case START_CONSUMER =>
      Consumer.committableSource(consumerSettings, Subscriptions.topics("test"))
        .mapAsync(1) { msg =>
          //TODO Add saving in cassandra
          val flight = SimpleSerializer.deserialize[Flight](msg.record.value())
            log.debug("BITCH OFfSsET {} ====> {}",msg.record.offset,flight )

          msg.committableOffset.commitScaladsl()
        }
        .runWith(Sink.ignore)
  }
}

object KafkaConsumer {
  val SERVICE_NAME = "KafkaConsumer"

  case object START_CONSUMER

  def props(consumerSettings : ConsumerSettings[Array[Byte], Array[Byte]])(implicit mat: ActorMaterializer) : Props = Props(new KafkaConsumer(consumerSettings))
}