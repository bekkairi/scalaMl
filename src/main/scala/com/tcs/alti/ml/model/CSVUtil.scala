package com.tcs.alti.ml.model

import java.io.File

import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader
import org.datavec.api.transform.TransformProcess
import org.datavec.api.transform.analysis.DataAnalysis
import org.datavec.api.transform.schema.Schema
import org.datavec.api.transform.transform.normalize.Normalize
import org.datavec.api.transform.transform.time.DeriveColumnsFromTimeTransform
import org.datavec.local.transforms.AnalyzeLocal
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.joda.time.{DateTimeFieldType, DateTimeZone}
import org.nd4j.linalg.dataset.DataSet

import scala.collection.JavaConverters._
import scala.io.Source

object CSVUtil {

  val miniBatchSize = 128
  val trainRatio = 0.8
  val delimiter = ","

  def computeData(file: String, transformProcess: TransformProcess, out:Int): List[(DataSet, DataSet)] = {

    val labels = Source.fromFile(file).getLines.next().split(delimiter)

    val recordReader = new CSVRecordReader(1, delimiter)
    import org.datavec.api.split.FileSplit
    recordReader.initialize(new FileSplit(new File(file)))

    recordReader.reset()

    val transformProcessRecordReader = new TransformProcessRecordReader(recordReader, transformProcess)


    val iterator = new RecordReaderDataSetIterator(transformProcessRecordReader, miniBatchSize, transformProcess.getFinalSchema.numColumns() - out, transformProcess.getFinalSchema.numColumns() - 1, true)

    var list: List[(DataSet, DataSet)] = List()

    while (iterator.hasNext) {
      val path = iterator.next
      path.shuffle()
      val allData = path.splitTestAndTrain(trainRatio)
      list = list ::: List((allData.getTrain, allData.getTest))

    }

    list
  }

  def categoricalLabel(recordReader: CSVRecordReader, columns: List[String]): (DataAnalysis, TransformProcess, Int) = {

    var map: Map[Int, Set[String]] = Map()
    var output = 1

    val regNumber = "(^[-+]?[0-9]*\\.?[0-9]+)$".r
    var pos = 0
    val schema = new Schema.Builder()

    val rows = recordReader.next().listIterator()

    var index = 0
    while (rows.hasNext) {
      val row = rows.next().toString.trim
      if (!columns(index).startsWith("date")) {
        val result = regNumber findFirstIn row
        result match {
          case Some(v) =>
          case _ => map = map + ((pos, Set()))
        }
      }
      pos = pos + 1
      index = index + 1
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
      }
      else if (columns(k).startsWith("date")) {
        schema.addColumnsString(columns(k))
      }
      else {
        schema.addColumnsDouble(columns(k))
      }

    }

    recordReader.reset()

    val analysis = AnalyzeLocal.analyze(schema.build(), recordReader)

    val transformProcess = new TransformProcess.Builder(schema.build)
    var set: Set[String] = Set()
    var poly = 2
    for (k <- 0 until columns.length) {
      if (map.contains(k)) {
        if (k == columns.length - 1) {
          transformProcess.categoricalToOneHot(columns(k))
          output = map(k).size
        }else{
          transformProcess.categoricalToInteger(columns(k))
        }
      } else if (columns(k).startsWith("date")) {
        transformProcess.stringToTimeTransform(columns(k), "YYYY-MM-DD", DateTimeZone.UTC)
        transformProcess.transform(new DeriveColumnsFromTimeTransform.Builder(columns(k))
          .addIntegerDerivedColumn(columns(k) + "_new", DateTimeFieldType.dayOfYear())
          .build())
        set = set + columns(k)
      }
      else if (k < columns.length - 1) {
        transformProcess.normalize(columns(k), Normalize.MinMax2, analysis)
        //doubleColumnsMathOp(columns(k) + "_new" ,MathOp.Multiply, computePoly( columns(k),poly):_*)
        //set = set + columns(k)
        poly = poly + 1
      }
    }

    for (column <- set) {
      transformProcess.removeColumns(column)
      transformProcess.renameColumn(column + "_new", column)

    }

    var newColumns=columns
    if (output>1){
      val last=columns.last
      val len=columns.size-1
      newColumns= newColumns.dropRight(1)
      for (c <-  map(len).toList){
        newColumns= newColumns :::List(last+s"[$c]")
      }
    }

    transformProcess.reorderColumns(newColumns: _*)

    (analysis, transformProcess.build(), output)
  }

}
