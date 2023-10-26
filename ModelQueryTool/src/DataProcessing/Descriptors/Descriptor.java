package DataProcessing.Descriptors;

import DataProcessing.FeaturePipelineContext;

import java.util.Random;

public interface Descriptor {
    int BIN_COUNT = 20;

    int SAMPLE_COUNT = 10000000;

    void process(FeaturePipelineContext context);

    String getKey();
}
