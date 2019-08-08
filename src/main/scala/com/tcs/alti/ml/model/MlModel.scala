package com.tcs.alti.ml.model

object MlType extends Enumeration {
  type MlType = Value
  val LINEAR_REGRESSION = Value
}

trait MlModel[M, T, TR, TE] {

  val miniBatchSize = 128
  val maxEpocs = 2000
  val learningRate = 0.01
  val trainRatio = 0.65

  val name: String

  def model: M

  def trainEvaluation: TR

  def transformer: T

  def testEvaluation: TE

  def loadFromFile(file: String)
}
