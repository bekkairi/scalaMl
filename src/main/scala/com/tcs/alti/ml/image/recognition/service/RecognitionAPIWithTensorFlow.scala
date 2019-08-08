package com.tcs.alti.ml.image.recognition.service

import java.io.File
import java.nio.file.Paths
import java.util.UUID.randomUUID

import com.tcs.alti.ml.image.recognition.dao.ImageDAO
import org.datavec.image.loader.NativeImageLoader
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.slf4j.LoggerFactory

object RecognitionAPIWithTensorFlow {

  val tensorFlowRecognitionAPI: TensorFlowRecognitionAPI = new TensorFlowRecognitionAPI()
  val faceDetectionImage: FaceDetectionImage = new FaceDetectionImage()
  val nativeImageLoader = new NativeImageLoader(160, 160, 3)
  val THRESHOLD: Double
  = 0.81
  private val LOGGER = LoggerFactory.getLogger(classOf[TensorFlowRecognitionAPI])

  def sameImage(userId: String, bytesImage: Array[Byte]): Boolean = {

    val fileFirstImage = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString + ".jpg"
    val fileSecondImage = System.getProperty("java.io.tmpdir") + File.separator ++ randomUUID().toString + ".jpg"
    try {

      val dataByteImage = ImageDAO.userImage(userId)


      val dataBaseMat = faceDetectionImage.base64ToINDArray(dataByteImage)
      val currentImageMat = faceDetectionImage.userImageToINDArray(bytesImage)


      val firstCall = transpose(dataBaseMat)
      val firstRet = tensorFlowRecognitionAPI.computeEmbeddingVector(firstCall._1, firstCall._2, firstCall._3)

      val secondCall = transpose(currentImageMat)
      val secondRet = tensorFlowRecognitionAPI.computeEmbeddingVector(secondCall._1, secondCall._2, secondCall._3)

      val dif = Nd4j.create(firstRet).distance2(Nd4j.create(secondRet))

      LOGGER.warn(" user " + userId + " dif " + dif)

      dif < THRESHOLD


    }
    finally {
      Paths.get(fileFirstImage).toFile.delete()
      Paths.get(fileSecondImage).toFile.delete()
    }


  }


  private def transpose(indArray: INDArray): (Array[Array[Float]], Array[Array[Float]], Array[Array[Float]]) = {
    val one = Nd4j.create(1, 160, 160)
    one.assign(indArray.get(NDArrayIndex.point(0), NDArrayIndex.point(2)))
    val two = Nd4j.create(1, 160, 160)
    two.assign(indArray.get(NDArrayIndex.point(0), NDArrayIndex.point(1)))
    val three = Nd4j.create(1, 160, 160)
    three.assign(indArray.get(NDArrayIndex.point(0), NDArrayIndex.point(0)))

    (normalize(one.reshape(160, 160)).toFloatMatrix, normalize(two.reshape(160, 160)).toFloatMatrix, normalize(three.reshape(160, 160)).toFloatMatrix)
  }


  private def normalize(read: INDArray) = read.div(255.0)


}
