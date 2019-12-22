package com.project.handlers

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.akka.streams._
import com.sksamuel.pulsar4s.{ConsumerConfig, MessageId, ProducerConfig, ProducerMessage, PulsarClient, Subscription, Topic}
import com.typesafe.scalalogging.LazyLogging
import org.apache.pulsar.client.api.Schema

trait StreamHandler extends LazyLogging {


  protected def pulsarProducer(brokers: String)(implicit system:ActorSystem,
                                                mat: Materializer,
                                                schema: Schema[Array[Byte]]) = {

    logger.info("Starting Producer...")
    println("start!")
    val client: PulsarClient = buildPulsarClient(brokers)

    val intopic = Topic("persistent://sample/standalone/ns1/in")
    val outtopic = Topic("persistent://sample/standalone/ns1/out")

    val producerFn = () => client.producer(ProducerConfig(intopic))

    Source.fromIterator(() => List("a","b","c","d", "e").map(_.getBytes).iterator)
      .map(string => ProducerMessage(string))
      .runWith(sink(producerFn))

    logger.info("End Producer...")
    println("end!")

  }


  private def buildPulsarClient(brokers: String): PulsarClient = {
    PulsarClient(brokers)

  }

  protected def pulsarConsumer(brokers: String)(implicit system:ActorSystem,
                                                mat: Materializer,
                                                schema: Schema[Array[Byte]]) = {

    val client = PulsarClient("pulsar://localhost:6650")

    val intopic = Topic("persistent://sample/standalone/ns1/in")
    val outtopic = Topic("persistent://sample/standalone/ns1/out")

    val consumerFn = () => client.consumer(ConsumerConfig(subscriptionName=Subscription("mysub"), Seq(intopic)))
    val producerFn = () => client.producer(ProducerConfig(outtopic))

    source(consumerFn, Some(MessageId.earliest))
      .map { consumerMessage =>
        println("message => " + consumerMessage.value.map(_.toChar).mkString)
        ProducerMessage(consumerMessage.data) }
      .to(sink(producerFn)).run()


    //println("end")

  }


}
