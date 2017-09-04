package com.kafkaflight


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import DomainObjects._
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer}
import akka.stream.scaladsl.{Sink}
import com.kafkaflight.PollingActor.GetRecentFlights
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

object Init extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()


  //TODO - get host and port from application config
  val producerSettings: ProducerSettings[Array[Byte], Array[Byte]] = ProducerSettings(system, new ByteArraySerializer, new ByteArraySerializer).withBootstrapServers("localhost:9092")


  val pollingActor = system.actorOf(PollingActor.props,PollingActor.SERVICE_NAME)
  // TODO - use akka router
  val kafkaProducer = system.actorOf(KafkaProducer.props(producerSettings),KafkaProducer.SERVICE_NAME)

  val scheduler = QuartzSchedulerExtension(system)
  scheduler.schedule("Every30Seconds", pollingActor, GetRecentFlights)




//  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
//    .withBootstrapServers("localhost:9092")
//    .withGroupId("group1")
//    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
////
//  val partition = 0
//  val fromOffset : Long = 116
//
//  val subscription = Subscriptions.assignmentWithOffset(
//    new TopicPartition("test", partition) -> fromOffset
//  )
//  val done =
//    Consumer.plainSource(consumerSettings, subscription)
//      .mapAsync(1)(record => {
//        println("RAW " + record)
//
//        Future{println("BITCH 3333" + SimpleSerializer.deserialize[Flight](record.value()))}
//      })
//      .runWith(Sink.ignore)
//
//
//  Consumer.committableSource(consumerSettings, Subscriptions.topics("test"))
//    .mapAsync(1) { msg =>
//      Future{
//        println("RAW " + msg)
//        println("BITCH ====> " + SimpleSerializer.deserialize[Flight](msg.record.value()))
//        msg
//      }
//    }
//    .mapAsync(1) { msg =>
//      msg.committableOffset.commitScaladsl()
//    }
//    .runWith(Sink.ignore)

}


