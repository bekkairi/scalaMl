package com.tcs.alti.ml.image.recognition.dao

import java.util.Base64

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.IOUtils
import org.scalatest.FlatSpec
import scalikejdbc._


class TestImageDAO extends FlatSpec {

  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession


  it should " return value" in {
    sql" delete from user_image_recognition where id='scala_test'".update().apply()
    val image1 = getClass.getClassLoader.getResource("images/mansour1_1.jpg")
    ImageDAO.addUserInBase64("scala_test", Base64.getEncoder.encode(IOUtils.toByteArray(image1.openStream())))
    assert(new String(ImageDAO.userImage("scala_test")).contains("/9j/4AAQSkZJRgABAQAAAQABAAD/"))
  }

  it should " return an error" in {
    intercept[IllegalArgumentException] {
      ImageDAO.userImage("scala_test_me_papa")
    }

  }

}
