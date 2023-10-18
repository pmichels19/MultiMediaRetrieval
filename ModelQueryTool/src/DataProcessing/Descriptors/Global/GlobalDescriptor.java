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
    private static final int BIN_COUNT = 20;

    protected static final int SAMPLE_COUNT = 1000000;

    @Override
    public void process(FeaturePipelineContext context) {
        float[] raw = calculateDescriptor(context);
        context.putData(getKey(), toHistogram(raw));
    }

    private float[] toHistogram(float[] values) {
        float bmin = Float.MAX_VALUE;
        float bmax = -Float.MAX_VALUE;
        for (float v : values) {
            if (v < bmin) bmin = v;
            if (v > bmax) bmax = v;
        }

        float step = ((float) BIN_COUNT) / (bmax - bmin);
        float[] bins = new float[BIN_COUNT];
        for (float value : values) {
            int binIdx = Math.min(BIN_COUNT - 1, (int) ((value - bmin) * step));
            bins[binIdx]++;
        }

        for (int i = 0; i < BIN_COUNT; i++) bins[i] /= (float) values.length;
        return bins;
    }

    protected abstract float[] calculateDescriptor(FeaturePipelineContext context);
}
