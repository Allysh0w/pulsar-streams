package com.project.handlers

import akka.Done
import akka.kafka.ConsumerMessage
import akka.stream.scaladsl.Flow
import akka.stream.stage.{AsyncCallback, GraphStageLogic, GraphStageWithMaterializedValue, InHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet, SinkShape}
import com.sksamuel.exts.Logging
import com.sksamuel.pulsar4s.{Producer, ProducerMessage}

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

class PulsarKafkaSinkGraphStage[T](createFn: () => (Producer[T], ConsumerMessage.CommittableMessage[String, Array[Byte]]))
//  extends GraphStageWithMaterializedValue[SinkShape[ProducerMessage[T]], Future[Done]]
  extends GraphStageWithMaterializedValue[SinkShape[ProducerMessage[T]], ConsumerMessage.CommittableMessage[String, Array[Byte]]]
    with Logging {

  private val in = Inlet.create[ProducerMessage[T]]("pulsar.in")
  override def shape: SinkShape[ProducerMessage[T]] = SinkShape.of(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, ConsumerMessage.CommittableMessage[String, Array[Byte]]) = {

    val promise = Promise[Done]()

    val logic: GraphStageLogic = new GraphStageLogic(shape) with InHandler {
      setHandler(in, this)

      implicit def context: ExecutionContextExecutor = super.materializer.executionContext

      var producer: Producer[T] = _
      var next: AsyncCallback[ProducerMessage[T]] = _
      var error: Throwable = _

      override def preStart(): Unit = {
        producer = createFn._1()
        next = getAsyncCallback { _ => pull(in) }
        pull(in)
      }


      override def onPush(): Unit = {
        try {
          val t = grab(in)
          logger.debug(s"Sending message $t")
          producer.sendAsync(t).onComplete {
            case Success(_) => next.invoke(t)
            case Failure(e) =>
              logger.error("Failing pulsar sink stage", e)
              failStage(e)
          }
        } catch {
          case e: Throwable =>
            logger.error("Failing pulsar sink stage", e)
            failStage(e)
        }
      }

      override def postStop(): Unit = {
        logger.debug("Graph stage stopping; closing producer")
        producer.flush()
        producer.close()
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        promise.tryFailure(ex)
      }

      override def onUpstreamFinish(): Unit = {
        promise.trySuccess(Done)
      }
    }

    (logic, createFn._2)
  }
}
