package com.kafkaflight

import java.util.UUID

import akka.Done
import com.datastax.driver.core._
import com.kafkaflight.DomainObjects.Flight
import org.slf4j.LoggerFactory

import scala.util._
import scala.concurrent._

class CassandraDao {
  import CassandraDatabase._
  import CassandraDao._


  private val log = LoggerFactory.getLogger(getClass)

  def createFlightRow(flight: Flight)(implicit ec: ExecutionContext): Future[Done] = {
    Future {

      Try(session.executeAsync(createStatement.bind(UUID.randomUUID().toString, flight.icao,flight.callSign.orNull,flight.origin_country)).getUninterruptibly) match {
        case Success(resultSet) =>
          Done
        case Failure(e) =>
          log.error(s"Failure to create row in cassandra! ${e.getMessage}")
          throw new Exception("Database Failure!")
      }
    }
  }


}

object CassandraDao {
  import CassandraDatabase._


  //Prepared statements
  lazy val createStatement = session.prepare("INSERT INTO flights(id, icao, callSign, originCountry) VALUES (?, ?, ?, ?);")
  lazy val readStatement = session.prepare("SELECT id,icao, callSign, originCountry FROM flights WHERE id = ?;")
  lazy val deleteStatement = session.prepare("DELETE FROM flights WHERE id = ?;")
  lazy val countStatement = session.prepare("SELECT COUNT(*) FROM flights WHERE id = ?;")


  def getCount(r: Row) : Long = {
    r.getLong("count")
  }
}
