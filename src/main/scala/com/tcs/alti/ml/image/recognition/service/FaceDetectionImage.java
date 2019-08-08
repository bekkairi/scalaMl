package com.tcs.alti.ml.image.recognition.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static com.tcs.alti.ml.image.recognition.service.TensorFlowRecognitionAPI.INPUT_SIZE;
import static java.util.UUID.randomUUID;

public class FaceDetectionImage {

    private static Logger LOGGER = LoggerFactory.getLogger(FaceDetectionImage.class);

    static {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final CascadeClassifier classifier;

    public FaceDetectionImage() {
        Config config = ConfigFactory.load("app.conf");
        classifier = new CascadeClassifier(config.getString("openCv.frontalface"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);


    }

    public INDArray userImageToINDArray(byte[] inputImage64) {

        String inputFileName = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString() + ".jpg";
        try {
            byte[] byteDecodeImage = Base64.getDecoder().decode(new String(inputImage64).replace("data:image/jpeg;base64,", ""));


            Files.copy(new ByteArrayInputStream(byteDecodeImage), Paths.get(inputFileName));
            return faceMat(inputFileName);


        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(" not able to detect the face for the given image ");
        } finally {
            new File(inputFileName).delete();
        }

    }

    public INDArray base64ToINDArray(byte[] imputImage64) {

        String inputFileName = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString() + ".jpg";
        try {
            byte[] byteDecodeImage = Base64.getDecoder().decode(new String(imputImage64).replace("data:image/jpeg;base64,", ""));

            Files.copy(new ByteArrayInputStream(byteDecodeImage), Paths.get(inputFileName));
            return new NativeImageLoader(INPUT_SIZE, INPUT_SIZE, 3).asMatrix(opencv_imgcodecs.imread(inputFileName));


        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(" not able to detect the face for the given image ");
        } finally {
            new File(inputFileName).delete();
        }

    }


    public String faceImageToBase64(byte[] imputImage64) {

        String inputFileName = System.getProperty("java.io.tmpdir") + File.separator + randomUUID().toString() + ".jpg";
        try {
            byte[] byteDecodeImage = Base64.getDecoder().decode(new String(imputImage64).replace("data:image/jpeg;base64,", ""));


            Files.copy(new ByteArrayInputStream(byteDecodeImage), Paths.get(inputFileName));

            Mat src = Imgcodecs.imread(inputFileName);

            MatOfRect faceDetections = new MatOfRect();
            classifier.detectMultiScale(src, faceDetections, 1.1, 3, 0, new Size(0, 0), new Size(900, 900));

            if (faceDetections.toArray().length < 1) {
                throw new RuntimeException(" No face found for the given image ");
            }

            Rect rect = faceDetections.toArray()[0];

            Mat markedImage = new Mat(src, rect);

            Size scaleSize = new Size(INPUT_SIZE, INPUT_SIZE);
            Mat resizeImage = new Mat(INPUT_SIZE, INPUT_SIZE, markedImage.type());

            Imgproc.resize(markedImage, resizeImage, scaleSize, 0, 0, Imgproc.INTER_CUBIC);

            Imgcodecs.imwrite(inputFileName, resizeImage);

            byte[] fileContent = FileUtils.readFileToByteArray(new File(inputFileName));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(" not able to detect the face for the given image ");
        } finally {
            new File(inputFileName).delete();
        }

    }


    private INDArray faceMat(String fileName) throws IOException {

        Mat src = Imgcodecs.imread(fileName);

        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(src, faceDetections, 1.1, 3, 0, new Size(0, 0), new Size(900, 900));

        if (faceDetections.toArray().length < 1) {
            throw new RuntimeException(" No face found for the given image ");
        }

        Rect rect = faceDetections.toArray()[faceDetections.toArray().length - 1];

        Mat markedImage = new Mat(src, rect);

        Size scaleSize = new Size(INPUT_SIZE, INPUT_SIZE);
        Mat resizeImage = new Mat(INPUT_SIZE, INPUT_SIZE, markedImage.type());

        Imgproc.resize(markedImage, resizeImage, scaleSize, 0, 0, Imgproc.INTER_CUBIC);

        Imgcodecs.imwrite(fileName, resizeImage);


        return new NativeImageLoader(INPUT_SIZE, INPUT_SIZE, 3).asMatrix(opencv_imgcodecs.imread(fileName));

    }


}
