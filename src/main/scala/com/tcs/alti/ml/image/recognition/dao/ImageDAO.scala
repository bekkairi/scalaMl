package com.tcs.alti.ml.image.recognition.dao

import com.tcs.alti.ml.image.recognition.service.FaceDetectionImage
import com.typesafe.config.{Config, ConfigFactory}
import scalikejdbc.{AutoSession, ConnectionPool, _}

object ImageDAO {

  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  val faceDetectionImage: FaceDetectionImage = new FaceDetectionImage()


  def addUserInBase64(id: String, bytesImage: Array[Byte]) = {


    sql"insert into user_image_recognition (id, image) values (${id}, ${faceDetectionImage.faceImageToBase64(bytesImage)})".update.apply()

  }

  def userImage(id: String): Array[Byte] = {

    val list: List[Option[Array[Byte]]] = sql"select image from user_image_recognition where id=${id}".map(rs => rs.bytesOpt("image")).list().apply()

    list.length == 0 match {
      case true => throw new IllegalArgumentException(s"User $id is not registred ")
      case false => list(0).get
    }
  }

  def userId(id: String): String = {

    val list: List[String] = sql"select image from user_image_recognition where id=${id}".map(rs => rs.string(1)).list().apply()
    list.length == 0 match {
      case true => throw new IllegalArgumentException(s"User $id is not registred ")
      case false => list(0)
    }
  }


}
