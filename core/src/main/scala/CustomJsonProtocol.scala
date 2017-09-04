package com.kafkaflight

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import DomainObjects._

object CustomJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  implicit object FlightJsonFormat extends RootJsonFormat[Flight] {
    def write(c: Flight) : JsValue = JsArray(JsString(c.icao), JsString(c.callSign.orNull), JsString(c.origin_country))

    def read(value: JsValue) : Flight= value match {

      case JsArray(Vector(JsString(icao), callSign, JsString(origin_country),_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>

        Flight(icao,callSign.convertTo[Option[String]],origin_country)

      case _ => deserializationError("Wrong json format!")
    }
  }

  implicit object PolledFlightsJsonFormat extends RootJsonFormat[PolledFlights] {
    def write(c: PolledFlights) : JsValue = JsArray(JsNumber(c.time),c.flights.toJson)

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

