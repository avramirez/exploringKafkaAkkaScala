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
    case GetRecentFlights => {
      log.debug("Poll baby poll")

      val responseFuture: Future[HttpResponse] = {
        Http().singleRequest(HttpRequest(HttpMethods.GET,Uri("https://opensky-network.org/api/states/all?icao24=aa3cbf")))
      }

      responseFuture onComplete {
        case Success(response) =>
          response match {
            case HttpResponse(StatusCodes.OK, headers, entity, _) =>

             Unmarshal(entity).to[String].foreach{ jsonString =>
               val flights = JsonParser(jsonString).convertTo[PolledFlights].flights
               if(flights.nonEmpty){
                _kafkaProducer ! PublishFlightMessage("test",Source(JsonParser(jsonString).convertTo[PolledFlights].flights))
               }else {
                 log.warning("Flights are empty! Not publishing any message!")
               }
              }

            case response => log.error(s"Invalid response! ${response.status}")

          }
        case Failure(e) => log.error(s"Failure in http request! ${e.getMessage}")
      }
    }

    case _ => log.error("Unknown Poll Operation")
  }
}

object PollingActor {
  def props(implicit mat: ActorMaterializer) = Props(new PollingActor())

  val SERVICE_NAME = "PollingActor"

  case object GetRecentFlights
}
