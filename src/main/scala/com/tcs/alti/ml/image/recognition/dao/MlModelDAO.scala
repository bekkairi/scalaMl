package com.tcs.alti.ml.image.recognition.dao

import java.io.{ByteArrayInputStream, File, FileInputStream}
import java.nio.file.{Files, Paths}
import java.util.Base64
import java.util.UUID.randomUUID

import com.tcs.alti.ml.model.MlType.MlType
import com.tcs.alti.ml.model.{CSVLinearRegressionModel, MlModel}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.compress.utils.IOUtils
import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
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

  def saveModel[T](name: String, mlTpye: MlType, mlModel: MlModel[T])(implicit encoder: Base64.Encoder) = {

    mlModel match {
      case CSVLinearRegressionModel(model: String, fileStatsStorage: Option[FileStatsStorage]) => {
        val currentModel = mlModel.asInstanceOf[CSVLinearRegressionModel]

        val zipFile = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString + ".zip"

        ModelSerializer.writeModel(currentModel.model, zipFile, true)

        val stToS64 = new String(encoder.encode(IOUtils.toByteArray(new FileInputStream(zipFile))))
        sql"insert into ml_model values (${name}, ${mlTpye.toString}, ${stToS64}, ${currentModel.trainEvaluation.toJson}, ${currentModel.testEvaluation.toJson},${currentModel.model.conf().toJson},${currentModel.model.gradient().toString},${currentModel.analysis.toJson},${currentModel.transformer.toJson})".update.apply()

      }

    }

  }

  def modelFromData[T](modelName: String): MlModel[T] = {


    val list =

      sql" select name, type, trainevaluation,testevaluation,model,transformer,analysis  from ml_model where name=${modelName}  ".map(
        rs => {
          val modelType = rs.string(2)

          modelType match {
            case "LINEAR_REGRESSION" =>
              modelFromJson(rs.string("name"), rs.string("trainevaluation"), rs.string("testevaluation"), rs.string("model"), rs.string("analysis"),
                rs.string("transformer")).asInstanceOf[MlModel[T]]
            case _ => throw new RuntimeException("Model not found")
          }
        }
      ).list().apply()


    return list(0)
  }


  def modelFromJson(modelName: String, trainEvaluation: String, testEvaluation: String, modelStream: String, analysis: String, transform: String): CSVLinearRegressionModel = {


    val linearReression = new CSVLinearRegressionModel(modelName, None)

    val byteModel = Base64.getDecoder().decode(modelStream);

    val zipFile = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString + ".zip"


    Files.copy(new ByteArrayInputStream(byteModel), Paths.get(zipFile));


    linearReression.someModel = Some(ModelSerializer.restoreMultiLayerNetwork(zipFile))
    linearReression.someTrainRegressionEvaluation = Some(BaseEvaluation.fromJson(trainEvaluation, classOf[RegressionEvaluation]))

    linearReression.someTestRegressionEvaluation = Some(BaseEvaluation.fromJson(testEvaluation, classOf[RegressionEvaluation]))

    linearReression.someAnalysis = Some(DataAnalysis.fromJson(analysis))
    linearReression.someTransformer = Some(TransformProcess.fromJson(transform))

    linearReression
  }


}
