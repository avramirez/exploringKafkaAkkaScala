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
    case PublishFlightMessage(topic,source ,producerSettings) =>

      source.map { s =>
        new ProducerRecord[Array[Byte], Array[Byte]](topic, SimpleSerializer.serialize(s))
      }.runWith(Producer.plainSink(producerSettings))


    case msg => log.error(s"Unsupported Kafka Message! Will not process message : $msg")


  }
}

object KafkaProducer {
  def props(implicit mat: ActorMaterializer) : Props = Props(new KafkaProducer)

  val actorName = "KafkaProducer"

  /**I tried to make a generic KafkaMessage and I ran out time  :P
    *
    *@param topic target topic to write into
    *@param source you can pass Iterable[T] scala implicit conversion will kick in to make it as Source
    *@param producerSettings producer settings to be used for Producer.plainSink
    *
    * */
  case class PublishFlightMessage(topic: String,source:Source[Flight, NotUsed],producerSettings : ProducerSettings[Array[Byte], Array[Byte]])
}
