package com.project.handlers

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

trait KafkaProducerHandler extends LazyLogging{

  def buildKafkaProducer(message: String,
                         kafkaTopic: String,
                         kafkaBroker: String,
                         kafkaConfig: Config)(implicit mat: ActorMaterializer): Future[Done] = {
    logger.info(s"Creating kafka producer on " +
      s"$kafkaBroker, " +
      s"topic $kafkaTopic"
    )

    Source.single(message)
      .map(value => new ProducerRecord[String, String](kafkaTopic, value))
      .runWith(Producer.plainSink(buildProducerSettings(kafkaBroker, kafkaConfig)))
  }

  def buildProducerSettings(kafkaBroker: String,
                            kafkaConfig: Config): ProducerSettings[String, String] = {

    ProducerSettings.create(kafkaConfig ,new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaBroker)
  }

}
