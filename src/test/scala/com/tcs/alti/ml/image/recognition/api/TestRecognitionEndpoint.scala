package com.tcs.alti.ml.image.recognition.api

import java.io.{File, FileWriter}
import java.util.Base64
import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, Multipart, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.tcs.alti.ml.image.recognition.dao.ImageDAO
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.IOUtils
import org.scalatest.{FlatSpec, Matchers}
import scalikejdbc._

import scala.concurrent.duration.DurationInt

class TestRecognitionEndpoint extends FlatSpec with Matchers with ScalatestRouteTest with RecognitionEndpoint {

  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(180.seconds)

  override def testConfigSource = "akka.loglevel = WARNING"


  it should "be able to upload file" in {

    val image1 = getClass.getClassLoader.getResource("images/img_1.jpg")
    val image2 = getClass.getClassLoader.getResource("images/img_1017.jpg")

    sql" delete from user_image_recognition where id='scala_test'".update().apply()
    ImageDAO.addUserInBase64("scala_test", Base64.getEncoder.encode(IOUtils.toByteArray(image1.openStream())))


    val file = File.createTempFile(randomUUID().toString, ".txt")
    val ret = Base64.getEncoder().encode(IOUtils.toByteArray(image2.openStream()))
    IOUtils.write(ret, new FileWriter(file))

    val formData = Multipart.FormData.fromFile("file", ContentTypes.`application/octet-stream`, file, 100000)
    Post(s"/user/recognition/scala_test", formData) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] contains "File successfully uploaded"
    }
  }
}
