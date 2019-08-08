package com.tcs.alti.ml.model

import java.util.Base64

import com.tcs.alti.ml.image.recognition.dao.MlModelDAO
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import scalikejdbc.{AutoSession, ConnectionPool}
import scalikejdbc._

class CSVLinearRegressionModelTest extends FunSuite {
  implicit val decoder = Base64.getEncoder
  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  test("first test") {
    val train = getClass.getClassLoader.getResource("model/regression/train.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train")
    cSVLinearRegressionModel.loadFromFile(train.getFile)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape()sameElements  Array(1, 2))
  }


  test("next test") {

    val train = getClass.getClassLoader.getResource("model/regression/notes.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train")
    cSVLinearRegressionModel.loadFromFile(train.getFile)
    sql" delete from ml_model where name='scala_test'".update().apply()
    MlModelDAO.saveModel("scala_test", MlType.LINEAR_REGRESSION, cSVLinearRegressionModel)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape()sameElements  Array(1, 33))

  }

}
