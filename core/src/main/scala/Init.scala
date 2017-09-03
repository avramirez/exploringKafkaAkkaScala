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
import DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

case class Flight(icao:String,callSign : Option[String],origin_country: String)
case class PolledFlights(time:Int,flights:Vector[Flight])

object MyJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  implicit object FlightJsonFormat extends RootJsonFormat[Flight] {
    def write(c: Flight) : JsValue=
      JsArray(JsString(c.icao), JsString(c.callSign.orNull), JsString(c.origin_country))

    def read(value: JsValue) : Flight= value match {
      case JsArray(Vector(JsString(icao), callSign, JsString(origin_country),_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
        Flight(icao,callSign.convertTo[Option[String]],origin_country)
      case _ => deserializationError("Wrong json format!")
    }
  }

  implicit object PolledFlightsJsonFormat extends RootJsonFormat[PolledFlights] {
    def write(c: PolledFlights) : JsValue =
      JsArray(JsNumber(c.time),null)

    def read(value: JsValue) : PolledFlights = {
      value.asJsObject.getFields("time","states") match {
        case Seq(JsNumber(time),states) =>
          val flights = states match {
            case JsNull => Vector.empty[Flight]
            case f => f.convertTo[Vector[Flight]]
          }
          PolledFlights(time.toInt,flights)
        case _ => deserializationError("Wrong json format!")
      }
    }
  }


}

object Init extends App {

  import MyJsonProtocol._

  println("Hello Worldz")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] = {
    Http().singleRequest(HttpRequest(HttpMethods.GET,Uri("https://opensky-network.org/api/states/all?icao24=aa8c39")))
  }



  val result = responseFuture.flatMap {
    case HttpResponse(StatusCodes.OK, headers, entity, _) => Unmarshal(entity).to[String].map {
      JsonParser(_).convertTo[PolledFlights]
    }
    case _ => null

  }

  result onComplete {
    case Success(s) => println("I AM Success wohooo " + s)
    case Failure(e) => e.printStackTrace()
  }


}


