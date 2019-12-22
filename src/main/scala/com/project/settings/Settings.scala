package com.project.settings

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContextExecutor

trait Settings {

  lazy private val appConf = ConfigFactory.load("application.conf")

  val mainConfig: Config = appConf.getConfig("stream").resolve()

  implicit val KafkaConfigProducer: Config = mainConfig.getConfig("akka.kafka.producer")
  implicit val system: ActorSystem = ActorSystem("System")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val pulsarBroker: String = mainConfig.getString("pulsarBroker")
  val pulsarTopic: String = mainConfig.getString("pulsarTopic")

  val kafkaBroker: String = mainConfig.getString("kafkaBroker")
  val kafkaTopic: String = mainConfig.getString("kafkaTopic")

  def kafkaSettings: Properties = {
    val properties = new Properties()
    properties.put("bootstrap.servers", kafkaBroker)
    properties.put("key.serializer", classOf[StringSerializer])
    properties.put("value.serializer", classOf[StringSerializer])
    properties
  }
}

object Settings extends Settings {

}
