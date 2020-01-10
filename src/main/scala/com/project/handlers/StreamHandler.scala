package com.project.handlers

import java.util.Properties

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage
import akka.stream.Materializer
import akka.stream.scaladsl.{RestartSource, Source}
import com.project.settings.Settings.BackoffSettings
import com.sksamuel.pulsar4s.akka.streams._
import com.sksamuel.pulsar4s.{ConsumerConfig, MessageId, ProducerConfig, ProducerMessage, PulsarClient, Subscription, Topic}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.pulsar.client.api.Schema

import scala.concurrent.{ExecutionContext, Future}

trait StreamHandler extends LazyLogging with KafkaConsumerHandler {


  protected def pulsarProducer(brokers: String,
                               message: String,
                               inTopic: String,
                               outTopic: String)(implicit system: ActorSystem,
                                                 mat: Materializer,
                                                 schema: Schema[Array[Byte]]) = {

    logger.info("Starting Producer...")
    val client: PulsarClient = buildPulsarClient(brokers)

    val intopic = Topic(inTopic)
    val outtopic = Topic(outTopic)

    val producerFn = () => client.producer(ProducerConfig(intopic))

    Source.fromIterator(() => List("a", "b", "c", "d", "e").map(_.getBytes).iterator)
      .map(string => ProducerMessage(string))
      .runWith(sink(producerFn))

    logger.info("End Producer...")

  }


  private def buildPulsarClient(brokers: String): PulsarClient = {
    PulsarClient(brokers)

  }

  protected def pulsarConsumer(brokers: String,
                               pulsarTopic: String)(implicit system: ActorSystem,
                                                    mat: Materializer,
                                                    schema: Schema[Array[Byte]]) = {

    val client = PulsarClient("pulsar://localhost:6650")

    val intopic = Topic("persistent://sample/standalone/ns1/in")
    val outtopic = Topic("persistent://sample/standalone/ns1/out")

    val consumerFn = () => client.consumer(ConsumerConfig(subscriptionName = Subscription("mysub"), Seq(intopic)))
    val producerFn = () => client.producer(ProducerConfig(outtopic))

    source(consumerFn, Some(MessageId.earliest))
      .map { consumerMessage =>
        val a: Array[Byte] = consumerMessage.data
        println("message => " + consumerMessage.value.map(_.toChar).mkString)
        ProducerMessage(consumerMessage.data)
      }
      .to(sink(producerFn)).run()


  }


  protected def kafkaToPulsar(pulsarBrokers: String,
                              kafkaBroker: String,
                              kafkaConfig: Config,
                              kafkaGroupId: String,
                              kafkaStartupMode: String,
                              kafkaTopic: String,
                              pulsarInTopic: String,
                              backoffSettings: BackoffSettings,
                              message: Array[Byte])(implicit system: ActorSystem,
                                                    mat: Materializer,
                                                    ec: ExecutionContext,
                                                    schema: Schema[Array[Byte]]) = {

    val client = PulsarClient(pulsarBrokers)
    val topic = Topic(pulsarInTopic)
    val producerFn = () => client.producer(ProducerConfig(topic))

    logger.info(s"Starting Kafka consumer on $kafkaBroker, topic $kafkaTopic")
    logger.info(s"Trasfering data from Kafka brokers: [ $kafkaBroker ] topic: $kafkaTopic to Pulsar brokers: [ $pulsarBrokers ] topic: $pulsarInTopic")

    RestartSource.withBackoff(
      minBackoff = backoffSettings.minBackoffSeconds,
      maxBackoff = backoffSettings.maxBackoffSeconds,
      randomFactor = backoffSettings.randomFactor
    ) { () =>

      streamKafkaConsumer(kafkaConfig, kafkaBroker, kafkaTopic, kafkaGroupId, kafkaStartupMode)
        .mapAsync(2) { message =>
            logger.info(s"Got message: ${message.record.value.map(_.toChar).mkString} from kafka from topic: $kafkaTopic")
 //           Future(ProducerMessage(message.record.value))
//            Future(message)
 //           .map

        }
    }



    //ProducerMessage(message)


  }


}
