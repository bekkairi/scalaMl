package com.tcs.alti.ml.image.recognition.dao

import java.io.{File, FileInputStream}
import java.util.Base64
import java.util.UUID.randomUUID

import com.tcs.alti.ml.model.MlType.MlType
import com.tcs.alti.ml.model.{CSVLinearRegressionModel, MlModel}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.compress.utils.IOUtils
import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.evaluation.BaseEvaluation
import org.nd4j.evaluation.regression.RegressionEvaluation
import scalikejdbc.{AutoSession, ConnectionPool, _}

object MlModelDAO {

  val config: Config = ConfigFactory.load("app.conf")


  Class.forName(config.getString("jdbc.driver"))
  ConnectionPool.singleton(config.getString("jdbc.url"), config.getString("jdbc.username"), config.getString("jdbc.password"))
  implicit val session = AutoSession
  implicit val decoder = Base64.getEncoder

  def saveModel [M, T, TR, TE, A](name: String, mlTpye: MlType, mlModel: MlModel[M, T, TR, TE, A])(implicit encoder: Base64.Encoder) = {

    mlModel match {
      case CSVLinearRegressionModel(model: String, fileStatsStorage: Option[FileStatsStorage]) => {
        val currentModel = mlModel.asInstanceOf[CSVLinearRegressionModel]

        val zipFile = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString + ".zip"

        ModelSerializer.writeModel(currentModel.model, zipFile, true)

        val stToS64 = new String(encoder.encode(IOUtils.toByteArray(new FileInputStream(zipFile))))
        sql"insert into ml_model values (${name}, ${mlTpye.toString}, ${stToS64}, ${currentModel.trainEvaluation.toJson}, ${currentModel.testEvaluation.toJson},${currentModel.model.conf().toJson},${currentModel.model.gradient()},${currentModel.analysis.toJson},${currentModel.transformer.toJson})".update.apply()

      }

    }

  }

 /* def modelFromData(modelName: String): List[MlModel[M, T, TR, TE, A]] = {


    val list =

      sql" select name, type, trainevaluation,testevaluation,modelconfig,gradient,transformer where name=${modelName} from ml_model ".map(
        rs => {
          val modelType = rs.string(1)

          modelType match {
            case "LINEAR_REGRESSION" =>
              modelFromJson(rs.string(1), rs.string(3), rs.string(4), rs.string(5), rs.string(6), rs.string(7),
                rs.string(8))
            case _ => throw new RuntimeException("Model not found")
          }
        }
      ).list().apply()


    return list
  }


  def modelFromJson(modelName: String, trainEvaluation: String, testEvaluation: String, modelConfig: String, gradient: String, analysis: String, transform: String): MlModel[M, T, TR, TE, A] = {


    val linearReression = new CSVLinearRegressionModel(modelName, None)

    linearReression.someModel = Some(new MultiLayerNetwork(MultiLayerConfiguration.fromJson(modelConfig)))
    linearReression.someTrainRegressionEvaluation = Some(BaseEvaluation.fromJson(trainEvaluation, classOf[RegressionEvaluation]))

    linearReression.someTestRegressionEvaluation = Some(BaseEvaluation.fromJson(testEvaluation, classOf[RegressionEvaluation]))

    linearReression.someAnalysis = Some(DataAnalysis.fromJson(analysis))
    linearReression.someTransformer = Some(TransformProcess.fromJson(transform))

    linearReression.asInstanceOf[MlModel[M, T, TR, TE, A]]
  }3*/


}
