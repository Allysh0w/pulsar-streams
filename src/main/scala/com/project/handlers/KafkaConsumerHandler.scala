package com.project.handlers

import akka.kafka.{ConsumerMessage, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import com.project.handlers.setup.SetupKafkaConsumer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

trait KafkaConsumerHandler extends LazyLogging with SetupKafkaConsumer{


  protected def streamKafkaConsumer(
                                     akkaKafkaConfig: Config,
                                     kafkaBroker: String,
                                     kafkaTopic: String,
                                     kafkaGroupId: String,
                                     kafkaStartupMode: String
                                   ): Source[ConsumerMessage.CommittableMessage[String, Array[Byte]], Consumer.Control] = {

    logger.info(s"Starting Kafka consumer on broker $kafkaBroker, topic $kafkaTopic with group id $kafkaGroupId")
    Consumer.committableSource(
      consumerSettings(
        akkaKafkaConfig: Config,
        kafkaBroker: String,
        kafkaGroupId: String,
        kafkaStartupMode: String
      ), Subscriptions.topics(kafkaTopic))
  }


}
