package com.project

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.project.handlers.StreamHandler
import org.apache.pulsar.client.api.Schema


object Main extends App with StreamHandler{
  println("ok")

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val schema: Schema[Array[Byte]] = Schema.BYTES

  println("prepare to producer..")
  //pulsarProducer("pulsar://localhost:6650")
//  pulsarConsumer("")

}
