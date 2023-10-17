package DataProcessing.Descriptors;

import DataProcessing.FeaturePipelineContext;

import java.util.Random;

public interface Descriptor {
    void process(FeaturePipelineContext context);

    String getKey();
}
