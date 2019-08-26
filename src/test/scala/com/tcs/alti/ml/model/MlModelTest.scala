package com.tcs.alti.ml.model

import org.scalatest.FunSuite

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
  }

  test ("rate"){

    val learingRate= new TestMlModel().learningRateEpocs

    assert(!learingRate.isEmpty)
  }

}
