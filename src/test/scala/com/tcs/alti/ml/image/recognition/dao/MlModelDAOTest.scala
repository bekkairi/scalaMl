package com.tcs.alti.ml.image.recognition.dao

import com.tcs.alti.ml.model.{CSVLinearRegressionModel, MlType}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import scalikejdbc.{AutoSession, ConnectionPool}

import com.tcs.alti.ml.image.recognition.dao.MlModelDAO
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import scalikejdbc.{AutoSession, ConnectionPool, _}



class MlModelDAOTest extends FunSuite {

  val config: Config = ConfigFactory.load("app.conf")

  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession

  test (" save model and get"){
    val train = getClass.getClassLoader.getResource("model/regression/notes.csv")
    val cSVLinearRegressionModel = new CSVLinearRegressionModel("train2", None)
    cSVLinearRegressionModel.loadFromFile(train.getFile)
    sql" delete from ml_model where name='train2'".update().apply()
    MlModelDAO.saveModel(cSVLinearRegressionModel.name, MlType.LINEAR_REGRESSION, cSVLinearRegressionModel)
    assert(cSVLinearRegressionModel.model.gradient().gradient().shape() sameElements Array(1, 33))

  }

}
