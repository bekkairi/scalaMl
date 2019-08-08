package com.tcs.alti.ml.boot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tcs.alti.ml.image.recognition.api.{CORSHandler, MlEnpointModel, RecognitionEndpoint}

import scala.util.{Failure, Success}


object BootAltiMl extends App with RecognitionEndpoint with CORSHandler with MlEnpointModel {

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  val log = system.log

  Http().bindAndHandle(corsHandler(routes ~ mlRoutes), "localhost", 9999).onComplete {
    case Success(b) => log.info(s"application is up and running at ${b.localAddress.getHostName}:${b.localAddress.getPort}")
    case Failure(e) => log.error(s"could not start application: {}", e.getMessage)
  }


}
