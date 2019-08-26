package com.tcs.alti.ml.image.recognition.api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.util.ByteString
import com.tcs.alti.ml.image.recognition.dao.ImageDAO
import com.tcs.alti.ml.image.recognition.service.RecognitionAPIWithTensorFlow
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

final case class ResultApp(ret: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val orderFormat = jsonFormat1(ResultApp)
}

trait RecognitionEndpoint extends Directives with JsonSupport {


  implicit val system: ActorSystem

  //implicit val addressFormat = jsonFormat5(Result.apply)

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  val routes = login ~ uploadFile ~ findUser

  def findUser: Route = {
    path("users" / Segments) {
      ids =>
        (get) {
          complete {
            new ResultApp(ImageDAO.userId(ids(0)))

          }
        }
    }
  }

  def login: Route = {
    path("user" / "recognition" / Segments) { id => {
      (post & entity(as[Multipart.FormData])) { fileData =>
        onComplete(processFile(id(0), fileData)) {
          case Success(value) => complete(new ResultApp(RecognitionAPIWithTensorFlow.sameImage(id(0), value).toString))
          case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))

        }
      }
    }
    }
  }

  def uploadFile: Route = {
    path("user" / "createAccount" / Segments) { id => {
      (post & entity(as[Multipart.FormData])) { fileData =>
        onComplete(processFile(id(0), fileData)) {
          case Success(value) => complete(new ResultApp(ImageDAO.addUserInBase64(id(0), value).toString))
          case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))


        }

      }
    }
    }
  }

  private def processFile(id: String, fileData: Multipart.FormData): Future[Array[Byte]] = {

    fileData.parts.mapAsync(1) { bodyPart ⇒
      def writeFileOnLocal(array: Array[Byte], byteString: ByteString): Array[Byte] = {
        val byteArray: Array[Byte] = byteString.toArray
        array ++ byteArray
      }

      bodyPart.entity.dataBytes.runFold(Array[Byte]())(writeFileOnLocal)

    }.runFold(Array[Byte]())((v, t) => v ++ t)
  }
}
