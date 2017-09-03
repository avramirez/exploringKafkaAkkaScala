package com.kafkaflight

object DomainObjects {
  case class Flight(icao:String,callSign : Option[String],origin_country: String)
  case class PolledFlights(time:Int,flights:Vector[Flight])
}