package com.tcs.alti.ml.image.recognition.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.File;
import java.io.IOException;


public class TensorFlowRecognitionAPI {

    public static final int INPUT_SIZE = 160;
    private static final String OUTPUT_NAME = "embeddings:0";
    private static final String PHASE_NAME = "phase_train:0";
    private static final String INPUT_NAME = "input:0";
    private static Logger LOGGER = LoggerFactory.getLogger(TensorFlowRecognitionAPI.class);
    private final Graph graph;


    public TensorFlowRecognitionAPI() {
        try {
            Config config = ConfigFactory.load("app.conf");
            graph = new Graph();
            graph.importGraphDef(FileUtils.readFileToByteArray(new File(config.getString("recognition.pbFile"))));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }


    public float[][] computeEmbeddingVector(float[][] one, float[][] two, float[][] three) {

        float[][][][] image = new float[1][INPUT_SIZE][INPUT_SIZE][3];
        for (int i = 0; i < INPUT_SIZE; i++)
            for (int j = 0; j < INPUT_SIZE; j++) {
                image[0][i][j] = new float[]{one[i][j], two[i][j], three[i][j]};

            }
        try (Session s = new Session(graph)) {


            Tensor input = Tensor.create(image);
            Tensor out = s.runner().feed(INPUT_NAME, input).feed(PHASE_NAME, Tensor.create(false)).fetch(OUTPUT_NAME).run().get(0);
            float[][] ret = new float[1][512];
            out.copyTo(ret);

            return ret;
        }

    }


}
