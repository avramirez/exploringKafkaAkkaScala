package com.kafkaflight


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import com.kafkaflight.DomainObjects.Flight
import com.kafkaflight.KafkaConsumer.START_CONSUMER
import com.kafkaflight.PollingActor.GetRecentFlights
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

/**
  * TODO - Project
  *
  * - Integrate sbt native-packager for containerization
  * - Create a docker-compose file for easy deployment :
  *     spotify:kafka (kafka container with zookeeper)
  *     cassandra - DONE
  *     self - ....
  * - Create UnitTest
  * - Create Integration Test
  * - Refactor - removed magic values. Get it from application config. ex: kafka host, kafka port, topics, etc..
  *
  * */

object Init extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  CassandraDatabase.createSchema

  val cassandraDao = new CassandraDao

  val pollingActor = system.actorOf(PollingActor.props,PollingActor.SERVICE_NAME)


  //TODO - get host and port from application config
  val producerSettings: ProducerSettings[Array[Byte], Array[Byte]] = ProducerSettings(system, new ByteArraySerializer, new ByteArraySerializer).withBootstrapServers("localhost:9092")
  // TODO - use akka router
  val kafkaProducer = system.actorOf(KafkaProducer.props(producerSettings),KafkaProducer.SERVICE_NAME)

  val scheduler = QuartzSchedulerExtension(system)
  scheduler.schedule("Every30Seconds", pollingActor, GetRecentFlights)
  //Initial Polling, akka-quartz-scheduler do not have start now
  pollingActor ! GetRecentFlights


  val consumerSettings: ConsumerSettings[Array[Byte], Array[Byte]] = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  val kafkaConsumer = system.actorOf(KafkaConsumer.props(cassandraDao,consumerSettings))
  kafkaConsumer ! START_CONSUMER

}


