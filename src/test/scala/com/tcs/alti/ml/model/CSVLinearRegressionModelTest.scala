package com.tcs.alti.ml.model

import java.util.Base64

import com.tcs.alti.ml.image.recognition.dao.MlModelDAO
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import scalikejdbc.{AutoSession, ConnectionPool, _}

class CSVLinearRegressionModelTest extends FunSuite {
  implicit val decoder = Base64.getEncoder
  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  test("first test") {
    val train = getClass.getClassLoader.getResource("model/regression/train.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train1", None)
    sql" delete from ml_model where name='train1'".update().apply()
    cSVLinearRegressionModel.loadFromFile(train.getFile)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape() sameElements Array(1, 2))
  }


  test("next test") {

    val train = getClass.getClassLoader.getResource("model/regression/notes.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train2", None)
    cSVLinearRegressionModel.loadFromFile(train.getFile)
    sql" delete from ml_model where name='train2'".update().apply()
    MlModelDAO.saveModel(cSVLinearRegressionModel.name, MlType.LINEAR_REGRESSION, cSVLinearRegressionModel)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape() sameElements Array(1, 33))

  }

  test("third test") {


    val train = getClass.getClassLoader.getResource("model/regression/computer_hardware_dataset.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train3", None)


    cSVLinearRegressionModel.loadFromFile(train.getFile)
    sql" delete from ml_model where name='train3'".update().apply()


    MlModelDAO.saveModel(cSVLinearRegressionModel.name, MlType.LINEAR_REGRESSION, cSVLinearRegressionModel)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape() sameElements Array(1, 10))

  }

  test("test four") {


    val train = getClass.getClassLoader.getResource("model/regression/istanbul_stock_dataset.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train4", None)


    cSVLinearRegressionModel.loadFromFile(train.getFile)
    sql" delete from ml_model where name='train4'".update().apply()


    MlModelDAO.saveModel(cSVLinearRegressionModel.name, MlType.LINEAR_REGRESSION, cSVLinearRegressionModel)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape() sameElements Array(1, 10))

  }

}
