package com.kafkaflight


import com.datastax.driver.core.{ ProtocolOptions, Cluster, Session }
import com.typesafe.config.ConfigFactory
//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

trait Connector {
  this: CassandraCluster =>

  private val config = ConfigFactory.load()

  implicit def space: String = config.getString("database.cassandra.keyspace")

  lazy val session: Session = cluster.connect(space)

}

trait CassandraCluster {
  def cluster: Cluster
}

trait ConfigCassandraCluster extends CassandraCluster {
  private val config = ConfigFactory.load()

  private val port = config.getInt("database.cassandra.port")
  private val username = config.getString("database.cassandra.username")
  private val password = config.getString("database.cassandra.password")
  private val hosts = config.getStringList("database.cassandra.hosts").asScala

  lazy val cluster: Cluster =
    Cluster.builder().
      addContactPoints(hosts: _*).
      withPort(port).
      withCredentials(username,password).
      build()
}

object CassandraDatabase extends ConfigCassandraCluster with Connector {

  def createSchema = {

    val globalSession = cluster.connect()
    globalSession.execute("""CREATE KEYSPACE IF NOT EXISTS kafkatest
      with replication = {
        'class' : 'SimpleStrategy',
        'replication_factor':1
     };""")
    globalSession.close()

    session.execute("""CREATE TABLE IF NOT EXISTS flights (
      id varchar PRIMARY KEY,
      icao varchar,
      callSign varchar,
      originCountry varchar
    );""")
  }
}
