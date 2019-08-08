package com.tcs.alti.ml.image.recognition.dao

import java.io.{File, FileInputStream}
import java.util.Base64
import java.util.UUID.randomUUID

import com.tcs.alti.ml.model.MlType.MlType
import com.tcs.alti.ml.model.{CSVLinearRegressionModel, MlModel}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.compress.utils.IOUtils
import org.deeplearning4j.util.ModelSerializer
import scalikejdbc.{AutoSession, ConnectionPool, _}

object MlModelDAO {

  val config: Config = ConfigFactory.load("app.conf")


  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession
  implicit val decoder = Base64.getEncoder

  def saveModel[M, T, TR, TE](name: String, mlTpye: MlType, mlModel: MlModel[M, T, TR, TE])(implicit encoder: Base64.Encoder) = {

    mlModel match {
      case CSVLinearRegressionModel(model: String) => {
        val currentModel = mlModel.asInstanceOf[CSVLinearRegressionModel]

        val zipFile = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString + ".zip"

        ModelSerializer.writeModel(currentModel.model, zipFile, true)

        val stToS64 = new String(encoder.encode(IOUtils.toByteArray(new FileInputStream(zipFile))))
        sql"insert into ml_model values (${name}, ${mlTpye.toString}, ${stToS64}, ${currentModel.trainEvaluation.toJson}, ${currentModel.testEvaluation.toJson},${currentModel.model.conf().toJson},${currentModel.model.gradient().toString})".update.apply()

      }

    }

  }

}
