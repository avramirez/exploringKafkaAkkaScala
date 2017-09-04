package com.kafkaflight

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.kafkaflight.DomainObjects.Flight
import org.apache.kafka.clients.producer.ProducerRecord

/**
  * ==Overview==
  * Producer abstraction of akka-kafka-producer
  *
  * Provides message queuing and asynchronous kafka writing (maybe? :P)
  *
  * */

class KafkaProducer(implicit mat: ActorMaterializer) extends Actor with ActorLogging{
  import KafkaProducer._
  def receive = {
    case KafkaMessage(topic,source:Source[Flight, NotUsed] ,producerSettings : ProducerSettings[Array[Byte], Array[Byte]]) =>

      source.map { s =>
        new ProducerRecord[Array[Byte], Array[Byte]](topic, SimpleSerializer.serialize(s))
      }.runWith(Producer.plainSink(producerSettings))


    case msg : KafkaMessage => log.error(s"Unsupported Kafka Message! Will not process message : $msg")


  }
}

object KafkaProducer {

  def props(implicit mat: ActorMaterializer) : Props = Props(new KafkaProducer)

  /**
    *
    *@param topic target topic to write into
    *@param source to be changed maybe to Iteratable
    *@param producerSettings producer settings to be used for Producer.plainSink
    *
    * */
  case class KafkaMessage(topic: String,source: Source[_,_],producerSettings : ProducerSettings[_,_])
}