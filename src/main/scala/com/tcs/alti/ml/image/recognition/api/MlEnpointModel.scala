package com.tcs.alti.ml.image.recognition.api

import java.io.{File, FileWriter}
import java.nio.charset.Charset
import java.util.Base64
import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.util.ByteString
import com.tcs.alti.ml.image.recognition.dao.MlModelDAO
import com.tcs.alti.ml.model.{CSVLinearRegressionModel, MlType}
import org.apache.commons.io.IOUtils

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait MlEnpointModel extends Directives with JsonSupport {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val decoder = Base64.getEncoder

  val mlRoutes = upoload

  def upoload: Route = {

    path("model" / Segments) {
      ids =>
        (post & entity(as[Multipart.FormData])) {
          filData => {
            onComplete(processFile(filData)) {
              case Success(value) => {
                val mlType = MlType.withName(ids(0))
                mlType match {
                  case MlType.LINEAR_REGRESSION => {
                    val file = File.createTempFile(randomUUID.toString, "csv")
                    IOUtils.write(value, new FileWriter(file), Charset.defaultCharset())
                    val mlModel = new CSVLinearRegressionModel(ids(1))
                    mlModel.loadFromFile(file.getAbsolutePath)
                    MlModelDAO.saveModel(ids(1), MlType.LINEAR_REGRESSION, mlModel)
                    return complete(new ResultApp(ids(1)))
                  }
                }
              }
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          }

        }
    }

  }

  private def processFile(fileData: Multipart.FormData): Future[Array[Byte]] = {

    fileData.parts.mapAsync(1) { bodyPart ⇒
      def writeFileOnLocal(array: Array[Byte], byteString: ByteString): Array[Byte] = {
        val byteArray: Array[Byte] = byteString.toArray
        array ++ byteArray
      }

      bodyPart.entity.dataBytes.runFold(Array[Byte]())(writeFileOnLocal)

    }.runFold(Array[Byte]())((v, t) => v ++ t)
  }
}
