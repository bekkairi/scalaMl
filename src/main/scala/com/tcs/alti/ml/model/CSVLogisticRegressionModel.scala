package com.tcs.alti.ml.model

import java.io.File
import java.nio.file.Files
import java.util.UUID.randomUUID

import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader
import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator
import org.nd4j.linalg.learning.config.{Adam, Nesterovs}
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import spray.json.{JsNumber, JsObject, JsString, JsValue, JsonParser, ParserInput}

import scala.collection.JavaConverters._

class CSVLogisticRegressionModel(override val name: String, val fileStatsStorage: Option[FileStatsStorage]) extends LogisticRegressionModel {

  var someTrainRegressionEvaluation: Option[Evaluation] = None
  var someTestRegressionEvaluation: Option[Evaluation] = None
  var someModel: Option[MultiLayerNetwork] = None
  var someTransformer: Option[TransformProcess] = None
  var someAnalysis: Option[DataAnalysis] = None

  override def loadFromFile(file: String) = {

    buildModel(file)

  }

  def buildModel(file: String): Unit = {

    import scala.io.Source
    val columns = Source.fromFile(file).getLines.next.split(CSVUtil.delimiter)

    val recordReader = new CSVRecordReader(1, CSVUtil.delimiter)
    import org.datavec.api.split.FileSplit
    recordReader.initialize(new FileSplit(new File(file)))

    val modelAnalysis = CSVUtil.categoricalLabel(recordReader, columns.toList)
    val output = modelAnalysis._2.getFinalSchema.getColumnMetaData

    val list = CSVUtil.computeData(file, modelAnalysis._2,modelAnalysis._3)

    val ret = learningRateEpocs.toList.par.map(el => computeModel(columns.toList, modelAnalysis._3, el, list)).toList.sortWith(_._1.score() < _._1.score())

    val currentModel = computeModel(columns.toList, modelAnalysis._3, (ret(0)._2, maxNumEpocs), list)._1

    val mergeDataSet=computeDataForTest(list)

    someTrainRegressionEvaluation = Some(currentModel.evaluate(new SingletonDataSetIterator(mergeDataSet._1)))
    someTestRegressionEvaluation = Some(currentModel.evaluate(new SingletonDataSetIterator(mergeDataSet._2)))

    someModel = Some(currentModel)
    someTransformer = Some(modelAnalysis._2)
    someAnalysis = Some(modelAnalysis._1)

  }

  def computeDataForTest(list: List[(DataSet, DataSet)]):(DataSet,DataSet) ={

   list.fold((new DataSet(),new DataSet()))((acc,e) => ( DataSet.merge( List(acc._1,e._1).asJava),DataSet.merge( List(acc._2,e._2).asJava)))

  }

  def computeModel(labels: List[String], output: Int, learningRateEpocs: (Double, Int), list: List[(DataSet, DataSet)]): (MultiLayerNetwork, Double) = {



    val model = new MultiLayerNetwork(computeModel(labels,output,learningRateEpocs))
    model.init()

    if (fileStatsStorage.isDefined) {
      model.setListeners(new StatsListener(fileStatsStorage.get, 1))
    }

    val score = new ScoreIterationListener(1000)
    model.setListeners(score)



    var numEpocs = 0
    while (numEpocs < learningRateEpocs._2) {

      for (train <- list) {
        model.fit(train._1)
      }
      numEpocs = numEpocs + 1
    }

    return (model, learningRateEpocs._1)
  }

  def computeModel(labels: List[String], output: Int, learningRateEpocs: (Double, Int)): MultiLayerConfiguration={
    new NeuralNetConfiguration.Builder()
      .seed(123)
      .weightInit(WeightInit.XAVIER)
      .updater(new Adam(learningRateEpocs._1))
      .list()
      .layer(new DenseLayer.Builder().nIn(labels.length - 1).nOut(output)
        .activation(Activation.SIGMOID)
        .build())
      .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
        .activation(Activation.SOFTMAX)
        .nIn(output).nOut(output).build())
      .build();
  }

  override def toJson: JsValue = {
    JsObject(Map("model" -> JsonParser(ParserInput(model.conf().toJson)).asJsObject,
      "train" -> JsonParser(ParserInput(trainEvaluation.toJson)).asJsObject, "test" -> JsonParser(ParserInput(testEvaluation.toJson)).asJsObject,
      "analysis" -> JsonParser(ParserInput(analysis.toJson)).asJsObject))

  }

  override def trainEvaluation: Evaluation = someTrainRegressionEvaluation.get

  override def testEvaluation: Evaluation = someTestRegressionEvaluation.get

  override def analysis: DataAnalysis = someAnalysis.get

  override def model: MultiLayerNetwork = someModel.get

  override def predict(input: String): JsValue = {

    val expected = input.split(CSVUtil.delimiter).last
    val recordReader = new CSVRecordReader(0, CSVUtil.delimiter)
    import org.datavec.api.split.FileSplit
    val fileName = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString + ".csv"

    import java.io.ByteArrayInputStream
    import java.nio.file.Paths
    Files.copy(new ByteArrayInputStream(input.getBytes()), Paths.get(fileName))

    recordReader.initialize(new FileSplit(new File(fileName)))

    val transformProcessRecordReader = new TransformProcessRecordReader(recordReader, this.transformer)

    val iterator = new RecordReaderDataSetIterator(transformProcessRecordReader, CSVUtil.miniBatchSize, analysis.getSchema.numColumns() - 1, analysis.getSchema.numColumns() - 1, true)

    JsObject(Map("expected" -> JsString(expected), "predicted" -> JsNumber(model.output(iterator).getFloat(0))))

  }

  override def transformer: TransformProcess = someTransformer.get

}
