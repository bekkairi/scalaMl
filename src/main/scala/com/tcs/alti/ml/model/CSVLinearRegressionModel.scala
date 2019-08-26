package com.tcs.alti.ml.model

import java.io.File

import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader
import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
import org.datavec.api.transform.schema.Schema
import org.datavec.api.transform.transform.normalize.Normalize
import org.datavec.api.transform.transform.time.DeriveColumnsFromTimeTransform
import org.datavec.local.transforms.AnalyzeLocal
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.joda.time.{DateTimeFieldType, DateTimeZone}
import org.nd4j.evaluation.regression.RegressionEvaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.collection.JavaConverters._
import scala.io.Source

case class CSVLinearRegressionModel(override val name: String, val fileStatsStorage: Option[FileStatsStorage]) extends LinearRegressionModel {


  val delimiter = ","
  val numLinesToSkip = 1

  var someTrainRegressionEvaluation: Option[RegressionEvaluation] = None
  var someTestRegressionEvaluation: Option[RegressionEvaluation] = None
  var someModel: Option[MultiLayerNetwork] = None
  var someTransformer: Option[TransformProcess] = None
  var someAnalysis: Option[DataAnalysis] = None


  override def trainEvaluation: RegressionEvaluation = someTrainRegressionEvaluation.get

  override def testEvaluation: RegressionEvaluation = someTestRegressionEvaluation.get

  override def transformer: TransformProcess = someTransformer.get

  override def analysis: DataAnalysis = someAnalysis.get

  override def loadFromFile(file: String) = {

    buildModel(file)

  }

  def buildModel(file: String): Unit = {

    val list = computeData(file)

    val ret = learningRateEpocs.toList.par.map(el => computeModel(el, list, file)).toList.sortWith(_._1.score() < _._1.score())

    val currentModel = computeModel((ret(0)._2, maxNumEpocs), list, file)._1


    someTrainRegressionEvaluation = Some(currentModel.evaluateRegression(new SingletonDataSetIterator(list(0)._1)))
    someTestRegressionEvaluation = Some(currentModel.evaluateRegression(new SingletonDataSetIterator(list(0)._2)))
    someModel = Some(currentModel)

  }

  def computeModel(learningRateEpocs: (Double, Int), list: List[(DataSet, DataSet)], file: String): (MultiLayerNetwork, Double) = {


    val labels = Source.fromFile(file).getLines.next().split(delimiter);


    val outputLayer: OutputLayer = new OutputLayer.Builder(LossFunction.SQUARED_LOSS)
      .nIn(labels.length - 1)
      .nOut(1).activation(Activation.IDENTITY)
      .build()

    val logisticRegressionConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(123).updater(new Adam(learningRateEpocs._1)).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).weightInit(WeightInit.ZERO).l2(0.9)
      .list()
      .layer(0, outputLayer)
      .build()

    val model = new MultiLayerNetwork(logisticRegressionConf)
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

  def computeData(file: String): List[(DataSet, DataSet)] = {

    val labels = Source.fromFile(file).getLines.next().split(delimiter)

    val recordReader = new CSVRecordReader(1, delimiter)
    import org.datavec.api.split.FileSplit
    recordReader.initialize(new FileSplit(new File(file)))

    val transformProcess = categoricalLabel(recordReader, labels.toList)
    someTransformer = Some(transformProcess)
    recordReader.reset()

    val transformProcessRecordReader = new TransformProcessRecordReader(recordReader, transformProcess)


    val iterator = new RecordReaderDataSetIterator(transformProcessRecordReader, miniBatchSize, labels.length - 1, labels.length - 1, true)

    var list: List[(DataSet, DataSet)] = List()

    while (iterator.hasNext) {
      val path = iterator.next
      path.shuffle()
      val allData = path.splitTestAndTrain(trainRatio)
      list = list ::: List((allData.getTrain, allData.getTest))

    }

    list
  }

  def categoricalLabel(recordReader: CSVRecordReader, columns: List[String]): TransformProcess = {

    var map: Map[Int, Set[String]] = Map()

    val regNumber = "(^[-+]?[0-9]*\\.?[0-9]+)$".r
    var pos = 0
    val schema = new Schema.Builder()

    val rows = recordReader.next().listIterator()

    var index = 0
    while (rows.hasNext) {
      val row = rows.next().toString.trim
      if (!columns(index).startsWith("date")) {
        val result = regNumber findFirstIn row
        result match {
          case Some(v) =>
          case _ => map = map + ((pos, Set()))
        }
      }
      pos = pos + 1
      index = index + 1
    }

    recordReader.reset()

    while (recordReader.hasNext) {
      val rows = recordReader.next().listIterator()
      pos = 0
      while (rows.hasNext) {
        val row = rows.next()
        if (map.contains(pos)) {
          val set = map(pos) + (row.toString)
          map = map + ((pos, set))
        }

        pos = pos + 1
      }

    }

    for (k <- 0 until columns.length) {
      if (map.contains(k)) {
        schema.addColumnCategorical(columns(k), map(k).toList.asJava)
      }
      else if (columns(k).startsWith("date")) {
        schema.addColumnsString(columns(k))
      }
      else {
        schema.addColumnsDouble(columns(k))
      }

    }

    recordReader.reset()

    val analysis = AnalyzeLocal.analyze(schema.build(), recordReader)
    someAnalysis = Some(analysis)


    val transformProcess = new TransformProcess.Builder(schema.build)
    var set: Set[String] = Set()
    for (k <- 0 until columns.length) {
      if (map.contains(k)) {
        transformProcess.categoricalToInteger(columns(k))
      } else if (columns(k).startsWith("date")) {
        transformProcess.stringToTimeTransform(columns(k), "YYYY-MM-DD", DateTimeZone.UTC)
        transformProcess.transform(new DeriveColumnsFromTimeTransform.Builder(columns(k))
          .addIntegerDerivedColumn(columns(k) + "_new", DateTimeFieldType.dayOfYear())
          .build())
        set = set + columns(k)
      }
      else if (k < columns.length - 1) {
        transformProcess.normalize(columns(k), Normalize.MinMax2, analysis)
      }
    }

    for (column <- set) {
      transformProcess.removeColumns(column)
      transformProcess.renameColumn(column + "_new", column)
    }

    transformProcess.build()
  }

  override def model: MultiLayerNetwork = someModel.get


}
