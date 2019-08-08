package com.tcs.alti.ml.model

import org.datavec.api.transform.TransformProcess
import org.deeplearning4j.eval.RegressionEvaluation
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork


trait LinearRegressionModel extends MlModel[MultiLayerNetwork, TransformProcess, RegressionEvaluation, RegressionEvaluation] {


}
