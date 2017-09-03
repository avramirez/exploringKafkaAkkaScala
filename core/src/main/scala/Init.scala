package com.kafkaflight

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
import com.kafkaflight.PollingActor.GetRecentFlights
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

object Init extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()


  val pollingActor = system.actorOf(PollingActor.props)

  val scheduler = QuartzSchedulerExtension(system)


  scheduler.schedule("Every30Seconds", pollingActor, GetRecentFlights)

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


