package DataProcessing;

import Basics.Mesh;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FeaturePipelineContext {
    private final Map<String, Float> elementaryMap;

    private final Map<String, float[]> globalMap;

    private Mesh mesh;

    public FeaturePipelineContext() {
        this(null);
    }

    public FeaturePipelineContext(Mesh mesh) {
        this.mesh = mesh;

        elementaryMap = new HashMap<>();
        globalMap = new HashMap<>();
    }

    public static FeaturePipelineContext fromJson(String filePath) {
        if (!filePath.endsWith(".json")) throw new IllegalArgumentException("Please provide a .json file.");

        File json = new File(filePath);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (IOException e) {
            System.err.println("Failed to read file " + filePath + ":");
            e.printStackTrace();
            return null;
        }

        FeaturePipelineContext context = new FeaturePipelineContext();
        Iterator<Map.Entry<String, JsonNode>> iterator = root.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> field = iterator.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isArray()) {
                int size = value.size();
                float[] hist = new float[size];
                for (int i = 0; i < size; i++) hist[i] = value.get(i).floatValue();
                context.putData(key, hist);
            } else if (value.isNumber()) {
                context.putData(key, value.floatValue());
            }
        }

        return context;
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
        globalMap.put(key, values);
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
}
