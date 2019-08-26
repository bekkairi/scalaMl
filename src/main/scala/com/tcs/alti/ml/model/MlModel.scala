package com.tcs.alti.ml.model

object MlType extends Enumeration {
  type MlType = Value
  val LINEAR_REGRESSION = Value
}

trait MlModel[M, T, TR, TE, A] {


  val learningRateForEpocs: Int = 3000
  val miniBatchSize = 512
  val learningRateEpocs: Array[(Double, Int)] = computeLearningRate(7).toArray
  val maxNumEpocs = 50000
  val trainRatio = 0.8


  val name: String

  def model: M

  def trainEvaluation: TR

  def transformer: T

  def testEvaluation: TE

  def loadFromFile(file: String) = {

  }

  def analysis: A

  def computeLearningRate(max: Int): List[(Double, Int)] = {

    val first = (1 to max).toList.map(e => (math.pow(10, -e), learningRateForEpocs))
    val second = (1 to max).toList.map(e => (5 * math.pow(10, -e), learningRateForEpocs))

    return first ++ second ++ List((0.7.toDouble, learningRateForEpocs), (0.9.toDouble, learningRateForEpocs), (1.toDouble, learningRateForEpocs), (2.toDouble, learningRateForEpocs),
      (3.toDouble, learningRateForEpocs))

  }
}
