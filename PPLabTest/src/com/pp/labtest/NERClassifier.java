package com.pp.labtest;

import opennlp.tools.postag.*;
import opennlp.tools.util.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class NERClassifier {


    public static void main(String[] args){
        POSModel model = train("c:\\ner\\input.txt");
        String[] tests = new String[] {"Wevioo recrute", "samsung mobile phones", " lcd 52 inch tv"};
        for (String item : tests) {
            doTagging(model, item);
        }
    }


    public static POSModel train(String filepath) {
        POSModel model = null;
        TrainingParameters parameters = TrainingParameters.defaultParams();
        parameters.put(TrainingParameters.ITERATIONS_PARAM, "100");

        try {
            try (InputStream dataIn = new FileInputStream(filepath)) {
                ObjectStream<String> lineStream = new PlainTextByLineStream(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return dataIn;
                    }
                }, StandardCharsets.UTF_8);
                ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);

                model = POSTaggerME.train("fr", sampleStream, parameters, new POSTaggerFactory());
                return model;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeToFile(POSModel model, String modelOutpath) {
        try (OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(modelOutpath))) {
            model.serialize(modelOut);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static POSModel getModel(String modelPath) {
        try {
            try (InputStream modelIn = new FileInputStream(modelPath)) {
                POSModel model = new POSModel(modelIn);
                return model;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void doTagging(POSModel model, String input) {
        input = input.trim();
        POSTaggerME tagger = new POSTaggerME(model);
        Sequence[] sequences = tagger.topKSequences(input.split(" "));
        for (Sequence s : sequences) {
            List<String> tags = s.getOutcomes();
            System.out.println(Arrays.asList(input.split(" ")) +" =>" + tags);
        }
    }
}
