package DataProcessing.Descriptors.Global;

import DataProcessing.Descriptors.Descriptor;
import DataProcessing.FeaturePipelineContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public abstract class GlobalDescriptor implements Descriptor {
    protected static final int SAMPLE_COUNT = 1000000;

    @Override
    public void process(FeaturePipelineContext context) {
        context.putData(getKey(), calculateDescriptor(context));
    }

    protected abstract float[] calculateDescriptor(FeaturePipelineContext context);
}
