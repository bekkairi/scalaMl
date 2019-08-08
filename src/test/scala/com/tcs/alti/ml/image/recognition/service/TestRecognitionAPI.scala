package com.tcs.alti.ml.image.recognition.service

import java.util.Base64

import com.tcs.alti.ml.image.recognition.dao.ImageDAO
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.IOUtils
import org.scalatest.FlatSpec
import scalikejdbc._

class TestRecognitionAPI extends FlatSpec {

  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession


  it should "user1 should return true " in {

    val image1 = getClass.getClassLoader.getResource("images/mansour1.jpg")
    val image2 = getClass.getClassLoader.getResource("images/mansour2.jpg")

    sql" delete from user_image_recognition where id='scala_test'".update().apply()

    ImageDAO.addUserInBase64("scala_test", Base64.getEncoder().encode(IOUtils.toByteArray(image1.openStream())))

    val input = Base64.getEncoder().encode(IOUtils.toByteArray(image2.openStream()))


    assert(RecognitionAPIWithTensorFlow.sameImage("scala_test",
      input) == true)

  }

  it should "user2 should return true " in {

    val image1 = getClass.getClassLoader.getResource("images/aldo1.jpg")
    val image2 = getClass.getClassLoader.getResource("images/aldo2.jpg")

    sql" delete from user_image_recognition where id='scala_test'".update().apply()

    ImageDAO.addUserInBase64("scala_test", Base64.getEncoder().encode(IOUtils.toByteArray(image1.openStream())))

    val input = Base64.getEncoder().encode(IOUtils.toByteArray(image2.openStream()))


    assert(RecognitionAPIWithTensorFlow.sameImage("scala_test",
      input) == true)

  }

  it should "user2 should return false " in {

    val image1 = getClass.getClassLoader.getResource("images/mansour1.jpg")
    val image2 = getClass.getClassLoader.getResource("images/aldo2.jpg")

    sql" delete from user_image_recognition where id='scala_test'".update().apply()

    ImageDAO.addUserInBase64("scala_test", Base64.getEncoder().encode(IOUtils.toByteArray(image1.openStream())))

    val input = Base64.getEncoder().encode(IOUtils.toByteArray(image2.openStream()))


    assert(RecognitionAPIWithTensorFlow.sameImage("scala_test",
      input) == false)

  }


}
