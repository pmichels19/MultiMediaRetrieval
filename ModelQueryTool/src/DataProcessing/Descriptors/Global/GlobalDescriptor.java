package DataProcessing.Descriptors.Global;

import DataProcessing.Descriptors.Descriptor;
import DataProcessing.FeaturePipelineContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jogamp.opengl.math.Vec3f;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public abstract class GlobalDescriptor implements Descriptor {
    @Override
    public void process(FeaturePipelineContext context) {
        context.putData(getKey(), calculateDescriptor(context));
    }

    float[] toHistogram(float[] values) {
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
