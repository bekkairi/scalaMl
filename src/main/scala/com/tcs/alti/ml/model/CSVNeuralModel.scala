package com.tcs.alti.ml.model

import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

class CSVNeuralModel (override val name: String,  override val fileStatsStorage: Option[FileStatsStorage], val layers:List[Int]) extends CSVLogisticRegressionModel(name,fileStatsStorage) {


  override def  computeModel(labels: List[String], output: Int, learningRateEpocs: (Double, Int)): MultiLayerConfiguration={
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


}
