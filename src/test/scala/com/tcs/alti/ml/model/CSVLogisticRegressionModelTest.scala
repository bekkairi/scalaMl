package com.tcs.alti.ml.model

import java.util.Base64

import com.tcs.alti.ml.image.recognition.dao.MlModelDAO
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import scalikejdbc.{AutoSession, ConnectionPool, _}

class CSVLogisticRegressionModelTest extends FunSuite {

  implicit val decoder = Base64.getEncoder
  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  test("first test") {
    val train = getClass.getClassLoader.getResource("model/logistic/car.data")
    val logisticRegressionModel = new CSVLogisticRegressionModel("train1", None)
    sql" delete from ml_model where name= ${logisticRegressionModel.name}".update().apply()
    logisticRegressionModel.loadFromFile(train.getFile)
    assert(logisticRegressionModel.model.gradient().gradient().shape() sameElements Array(1, 4))
  }

}
