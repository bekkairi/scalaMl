package com.tcs.alti.ml.image.recognition.utils

import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.{Mat, Size}


object ImageUtil {


  def resizeImage(input: Mat): Mat = {
    val scaleSize = new Size(96, 96)
    val resizeImage = new Mat

    opencv_imgproc.resize(input, resizeImage, scaleSize)
    resizeImage
  }

}
