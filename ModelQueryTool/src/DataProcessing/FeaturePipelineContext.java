package DataProcessing;

import Basics.Mesh;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FeaturePipelineContext {
    private static final int BIN_COUNT = 20;

    private final Map<String, Float> elementaryMap;

    private final Map<String, float[]> globalMap;

    private Mesh mesh;

    public FeaturePipelineContext(Mesh mesh) {
        this.mesh = mesh;

        elementaryMap = new HashMap<>();
        globalMap = new HashMap<>();
    }

    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        for (String key : getElementaryKeys()) result.put(key, elementaryMap.get(key));
        for (String key : getGlobalKeys()) {
            ArrayNode arrayNode = mapper.createArrayNode();
            for (float v : globalMap.get(key)) arrayNode.add(v);
            result.set(key, arrayNode);
        }

        return result;
    }

    public void normalizeElementaries(FeatureStatistics statistics) {
        Set<String> keys = new HashSet<>(getElementaryKeys());
        for (String key : keys) {
            float currValue = elementaryMap.remove(key);

            float min = statistics.getMin(key);
            float max = statistics.getMax(key);
            float minmax = (currValue - min) / (max - min);
            elementaryMap.put(key + "_minmax", minmax);

            float mean = statistics.getMean(key);
            float stdev = statistics.getStdev(key);
            float standardized = (currValue - mean) / stdev;
            elementaryMap.put(key + "_standardized", standardized);
        }
    }

    public void putData(String key, float value) {
        elementaryMap.put(key, value);
    }

    public float getElementary(String key) {
        return elementaryMap.get(key);
    }

    public Set<String> getElementaryKeys() {
        return elementaryMap.keySet();
    }

    public void putData(String key, float[] values) {
        globalMap.put(key, toHistogram(values));
    }

    public float[] getGlobal(String key) {
        return globalMap.get(key);
    }

    public Set<String> getGlobalKeys() {
        return globalMap.keySet();
    }

    public Mesh getMesh() {
        if (mesh == null) throw new RuntimeException("Pipeline is being run with a null mesh.");
        return mesh;
    }

    public void unloadMesh() {
        mesh = null;
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
}
