package com.kafkaflight

import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import spray.json._
import CustomJsonProtocol._
import DomainObjects._
import akka.NotUsed
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Sink, Source}
import com.kafkaflight.KafkaProducer.KafkaMessage
import com.kafkaflight.PollingActor.GetRecentFlights
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

object Init extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()


  val pollingActor = system.actorOf(PollingActor.props)

  val scheduler = QuartzSchedulerExtension(system)


  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
//
  val partition = 0
  val fromOffset : Long = 100

  val subscription = Subscriptions.assignmentWithOffset(
    new TopicPartition("test", partition) -> fromOffset
  )
  val done =
    Consumer.plainSource(consumerSettings, subscription)
      .mapAsync(1)(record => {
        println("RAW " + record)

        Future{println("BITCH 3333" + SimpleSerializer.deserialize[Flight](record.value()))}
      })
      .runWith(Sink.ignore)

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
//
//
//
//
//
//
//  val fuckshit = Flight("flightId1",Some("fligh1"),"destination1")
//
//  val sereee = SimpleSerializer.serialize(fuckshit)
//  val backtoMyself = SimpleSerializer.deserialize[Flight](sereee)
//
//  println("AM I BACK? BITCH?" + backtoMyself)
//
//
//
//  val producerSettings: ProducerSettings[Array[Byte], Array[Byte]] = ProducerSettings(system, new ByteArraySerializer, new ByteArraySerializer)
//    .withBootstrapServers("localhost:9092")
//  val testList: Source[Flight, NotUsed] = Source(List(Flight("flightId5",Some("fligh5"),"destination5"),Flight("flightId6",Some("fligh6"),"destination6")))
//
//  // TODO - use akka router
//  val kafkaProducer = system.actorOf(KafkaProducer.props,"KafkaProducer")
//
//  kafkaProducer ! KafkaMessage("test",testList,producerSettings)


//
//  testList.map { fl =>
//    new ProducerRecord[Array[Byte], Array[Byte]]("test", SimpleSerializer.serialize(fl))
//  }.runWith(Producer.plainSink(producerSettings))


//  val done = Source(1 to 100)
//    .map(_.toString)
//    .map { elem =>
//      new ProducerRecord[Array[Byte], String]("test", elem)
//    }
//    .runWith(Producer.plainSink(producerSettings))
//
//  scheduler.schedule("Every30Seconds", pollingActor, GetRecentFlights)
//
//  val responseFuture: Future[HttpResponse] = {
//    Http().singleRequest(HttpRequest(HttpMethods.GET,Uri("https://opensky-network.org/api/states/all?icao24=aa8c39")))
//  }
//
//
//  val result = responseFuture.flatMap {
//    case HttpResponse(StatusCodes.OK, headers, entity, _) => Unmarshal(entity).to[String].map {
//      JsonParser(_).convertTo[PolledFlights]
//    }
//    case _ => null
//
//  }
//
//  result onComplete {
//    case Success(s) => println("I AM Success wohooo " + s)
//    case Failure(e) => e.printStackTrace()
//  }


}


