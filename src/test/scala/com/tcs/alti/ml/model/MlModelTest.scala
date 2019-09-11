package com.tcs.alti.ml.model

import org.datavec.api.transform.analysis.DataAnalysis
import org.datavec.api.transform.analysis.columns.ColumnAnalysis
import org.datavec.api.transform.{DataAction, TransformProcess}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.scalatest.FunSuite
import spray.json.{JsObject, JsString, JsonParser, ParserInput}

import scala.collection.JavaConverters._

class MlModelTest extends FunSuite {


  class TestMlModel extends MlModel[String] {
    override val name: String = ""
    override val toJson = JsonParser(ParserInput("{\"me\":1}")).asJsObject

    override def model = new MultiLayerNetwork("conf", null)

    override def trainEvaluation: String = ""

    override def transformer = new TransformProcess(null, List[DataAction]().asJava)

    override def testEvaluation: String = ""

    override def loadFromFile(file: String): Unit = {}

    override def analysis = new DataAnalysis(null, List[ColumnAnalysis]().asJava)

    def predict(input: String) = JsObject(Map("expected" -> JsString("ME")))
  }

  test("rate") {

    val learingRate = new TestMlModel().learningRateEpocs

    assert(!learingRate.isEmpty)
  }

}
