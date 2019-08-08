package com.tcs.alti.ml.model

import java.io.File

import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader
import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.schema.Schema
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.datasets.iterator.impl.SingletonDataSetIterator
import org.deeplearning4j.eval.RegressionEvaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.collection.JavaConverters._
import scala.io.Source

case class CSVLinearRegressionModel(override val name: String) extends LinearRegressionModel {

  val delimiter = ","
  val numLinesToSkip = 1

  var someTrainRegressionEvaluation: Option[RegressionEvaluation] = None
  var someTestRegressionEvaluation: Option[RegressionEvaluation] = None
  var someModel: Option[MultiLayerNetwork] = None
  var someTransformaer: Option[TransformProcess] = None


  override def trainEvaluation: RegressionEvaluation = someTrainRegressionEvaluation.get

  override def testEvaluation: RegressionEvaluation = someTestRegressionEvaluation.get

  override def model: MultiLayerNetwork = someModel.get

  override def transformer: TransformProcess = someTransformaer.get

  override def loadFromFile(file: String) = {
    val recordReader = new CSVRecordReader(1, delimiter)
    import org.datavec.api.split.FileSplit
    recordReader.initialize(new FileSplit(new File(file)))
    buildModel(recordReader, Source.fromFile(file).getLines.next().split(delimiter))

  }

  def buildModel(recordReader: CSVRecordReader, labels: Array[String]): Unit = {

    val transformProcess = categoricalLabel(recordReader, labels.toList)
    someTransformaer = Some(transformProcess)
    recordReader.reset()
    val transformProcessRecordReader = new TransformProcessRecordReader(recordReader, transformProcess)


    val outputLayer: OutputLayer = new OutputLayer.Builder(LossFunction.SQUARED_LOSS)
      .nIn(labels.length - 1)
      .nOut(1).activation(Activation.IDENTITY)
      .build()

    val logisticRegressionConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(123).updater(new Adam(this.learningRate)).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .list()
      .layer(0, outputLayer)
      .build()

    val model = new MultiLayerNetwork(logisticRegressionConf)
    model.init()
    val score = new ScoreIterationListener(500)
    model.setListeners(score)


    val iterator = new RecordReaderDataSetIterator(transformProcessRecordReader, miniBatchSize, labels.length - 1, labels.length - 1, true)


    //val normalizerTrain = new NormalizerStandardize
    //normalizerTrain.fit(iterator)

    var numEpocs = 0
    var trainEvaluation: RegressionEvaluation = null
    var testEvaluation: RegressionEvaluation = null

    while (numEpocs < this.maxEpocs) {
      while (iterator.hasNext) {
        val path = iterator.next
        val allData = path.splitTestAndTrain(trainRatio)
        val trainingData = allData.getTrain
        val testData = allData.getTest
        // normalizerTrain.transform(trainingData)
        //normalizerTrain.transform(testData)
        model.fit(trainingData)
        testEvaluation = model.evaluateRegression(new SingletonDataSetIterator(testData))
        trainEvaluation = model.evaluateRegression(new SingletonDataSetIterator(trainingData))
      }
      iterator.reset()

      numEpocs = numEpocs + 1

    }

    someTrainRegressionEvaluation = Some(trainEvaluation)
    someTestRegressionEvaluation = Some(testEvaluation)
    someModel = Some(model)

  }

  def categoricalLabel(recordReader: CSVRecordReader, columns: List[String]): TransformProcess = {

    var map: Map[Int, Set[String]] = Map()

    val regNumber = "\\D".r
    var pos = 0
    val schema = new Schema.Builder()

    val rows = recordReader.next().listIterator()

    while (rows.hasNext) {
      val row = rows.next().toString
      val result = regNumber findFirstIn row
      result match {
        case Some(v) => map = map + ((pos, Set()))
        case _ =>
      }
      pos = pos + 1
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
      } else {
        schema.addColumnsDouble(columns(k))
      }

    }

    val transformProcess = new TransformProcess.Builder(schema.build)
    for (k <- 0 until columns.length) {
      if (map.contains(k)) {
        transformProcess.categoricalToInteger(columns(k))
      }
    }

    transformProcess.build()
  }


}
