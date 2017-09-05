package com.kafkaflight

import java.util.UUID

import akka.Done
import com.datastax.driver.core._
import com.kafkaflight.DomainObjects.Flight

import scala.util._
import scala.concurrent._

class CassandraDao {
  import CassandraDatabase._
  import CassandraDao._


  def createFlightRow(flight: Flight)(implicit ec: ExecutionContext): Future[Done] = {
    Future {

      Try(session.executeAsync(createStatement.bind(UUID.randomUUID().toString, flight.icao,flight.callSign.orNull,flight.origin_country)).getUninterruptibly) match {
        case Success(resultSet) =>
          Done
        case Failure(e) =>
          //Log failure
          Done
      }
    }
  }

//  def getFlight(id: String)(implicit ec: ExecutionContext): Future[DbFlight] = {
//
//    Future {
//      Try(session.executeAsync(readStatement.bind(id)).getUninterruptibly) match {
//        case Success(resultSet) =>
//          Option(resultSet.one()) match {
//            case Some(row) => DbFlight(row)
//            case None      => null
//          }
//        case Failure(e) =>
//          //throw Exception
//      }
//    }
//  }
//
//  def deleteflight(id: String)(implicit ec: ExecutionContext): Future[Done] = {
//    Future {
//      Try(session.executeAsync(deleteStatement.bind(id)).getUninterruptibly) match {
//        case Success(resultSet) =>
//
//        case Failure(e) =>
//
//      }
//    }
//  }

//  def checkIfExist(id: String)(implicit ec: ExecutionContext): Future[Boolean] = {
//    Future {
//      Try(session.executeAsync(countStatement.bind(id)).getUninterruptibly) match {
//        case Success(resultSet) =>
//          proccessCountResultSet(resultSet)
//        case Failure(e) =>
//
//      }
//    }
//  }

  def proccessCountResultSet(resultSet: ResultSet): Boolean = {
    Option(resultSet.one()) match {
      case Some(frow) => {
       if (CassandraDao.getCount(frow) > 0) true else false

      }
      case None =>
        false
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

  case class DbFlight(id:String,icao:String,callSign : Option[String],origin_country: String)
  object DbFlight {
    def apply(r: Row): DbFlight = {
      DbFlight(r.getString("id"), r.getString("icao"),Option(r.getString("callSign")),r.getString("originCountry"))
    }
  }

  def getCount(r: Row) : Long = {
    r.getLong("count")
  }
}
