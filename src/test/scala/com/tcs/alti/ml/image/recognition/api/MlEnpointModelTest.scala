package com.tcs.alti.ml.image.recognition.api

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, Multipart, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import scalikejdbc._
import org.scalatest.Matchers._

import scala.concurrent.duration.DurationInt

class MlEnpointModelTest extends FunSuite with ScalatestRouteTest with MlEnpointModel {

  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(200.seconds)

  test("upload model") {
    sql" delete from ml_model where name='scala_test'".update().apply()
    val train = getClass.getClassLoader.getResource("model/regression/train.csv")

    val formData = Multipart.FormData.fromFile("file", ContentTypes.`application/octet-stream`, new File(train.getFile), 100000)
    val ret = Post(s"/model/LINEAR_REGRESSION/scala_test", formData)  ~> mlRoutes ~> check{
      status shouldEqual    StatusCodes.OK
    }

    println(ret)
  }


}
