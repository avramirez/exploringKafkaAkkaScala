package com.kafkaflight

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.kafka.ProducerSettings
import com.kafkaflight.DomainObjects.PolledFlights
import spray.json.JsonParser
import CustomJsonProtocol._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.kafkaflight.KafkaProducer.PublishFlightMessage
import org.apache.kafka.common.serialization.ByteArraySerializer
import scala.concurrent.ExecutionContext.Implicits.global

class PollingActor(implicit mat: ActorMaterializer) extends Actor with ActorLogging with WithKafkaProducer{

  import PollingActor._

  implicit val system = context.system

  def receive = {
    case GetRecentFlights =>
      log.debug("Poll baby poll")

      val responseFuture: Future[HttpResponse] = {
        Http().singleRequest(HttpRequest(HttpMethods.GET,Uri("https://opensky-network.org/api/states/all?icao24=4240eb")))
      }

      responseFuture onComplete {
        case Success(response) =>
          response match {
            case HttpResponse(StatusCodes.OK, headers, entity, _) =>
              log.debug("RESPONSE OK")
              pipe(Unmarshal(entity).to[String].map(JsonParser(_).convertTo[PolledFlights])) to self
            case response => log.error(s"Invalid response! ${response.status}")

          }
        case Failure(e) => log.error(s"Failure in http request! ${e.getMessage}")
      }

    case polledFlights : PolledFlights =>
      _kafkaProducer ! PublishFlightMessage("test",Source(polledFlights.flights))

    case _ => log.error("Unknown Poll Operation")
  }
}

object PollingActor {
  def props(implicit mat: ActorMaterializer) = Props(new PollingActor())

  val SERVICE_NAME = "PollingActor"

  case object GetRecentFlights
}
