package com.project.handlers.setup

import com.typesafe.scalalogging.LazyLogging

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

trait SetupKafkaConsumer extends LazyLogging {


  protected def consumerSettings(
                                akkaKafkaConfig: Config,
                                kafkaBroker: String,
                                kafkaGroupId: String,
                                kafkaStartupMode: String
                              ): ConsumerSettings[String, Array[Byte]] = {
    ConsumerSettings(akkaKafkaConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(kafkaBroker)
      .withGroupId(kafkaGroupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaStartupMode) // default = earliest
  }

}
