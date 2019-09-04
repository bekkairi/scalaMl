package com.tcs.alti.ml.model

import org.scalatest.FunSuite
import spray.json.{JsObject, JsString, JsonParser, ParserInput}

class MlModelTest extends FunSuite {


  class TestMlModel extends MlModel[String,String,String,String,String]{
    override val name: String = ""

    override def model: String = ""

    override def trainEvaluation: String = ""

    override def transformer: String= ""

    override def testEvaluation: String = ""

    override def loadFromFile(file: String): Unit = {}

    override def analysis: String =  ""

    override val miniBatchSize: Int = 12

    override val toJson=  JsonParser(ParserInput("me:me ")).asJsObject

    def  predict(input: String)=  JsObject ( Map( "expected" ->  JsString("ME")))
  }

  test ("rate"){

    val learingRate= new TestMlModel().learningRateEpocs

    assert(!learingRate.isEmpty)
  }

}
