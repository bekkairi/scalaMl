package com.tcs.alti.ml.model

import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import spray.json.JsValue

object MlType extends Enumeration {
  type MlType = Value
  val LINEAR_REGRESSION = Value
}

trait MlModel[T] {


  val learningRateForEpocs: Int = 5000
  val learningRateEpocs: Array[(Double, Int)] = computeLearningRate(7).toArray
  val maxNumEpocs = 30000


  val name: String

  def model: MultiLayerNetwork

  def trainEvaluation: T

  def transformer: TransformProcess

  def testEvaluation: T

  def loadFromFile(file: String) = {

  }

  def toJson: JsValue

  def analysis: DataAnalysis

  def computeLearningRate(max: Int): List[(Double, Int)] = {

    val first = (1 to max).toList.map(e => (math.pow(10, -e), learningRateForEpocs))
    val second = (1 to max).toList.map(e => (5 * math.pow(10, -e), learningRateForEpocs))

    return first ++ second ++ List((0.7.toDouble, learningRateForEpocs), (0.9.toDouble, learningRateForEpocs), (1.toDouble, learningRateForEpocs), (2.toDouble, learningRateForEpocs),
      (3.toDouble, learningRateForEpocs))

  }

  def predict(input: String): JsValue
}
