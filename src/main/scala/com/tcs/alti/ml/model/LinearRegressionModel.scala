package com.tcs.alti.ml.model

import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.evaluation.regression.RegressionEvaluation


trait LinearRegressionModel extends MlModel[MultiLayerNetwork, TransformProcess, RegressionEvaluation, RegressionEvaluation, DataAnalysis] {


}
