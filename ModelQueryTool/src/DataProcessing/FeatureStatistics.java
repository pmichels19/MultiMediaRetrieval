package DataProcessing;

import Basics.Helpers;
import DataProcessing.Descriptors.Descriptor;
import DataProcessing.Descriptors.Global.GlobalDescriptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FeatureStatistics {
    private final Map<String, Float> mins;
    private final Map<String, Float> maxs;
    private final Map<String, Float> means;
    private final Map<String, Float> stdevs;

    private Set<String> elementaryKeys;

    private Set<String> globalKeys;

    private static final String statisticsFile = "src\\DataProcessing\\statistics.json";

    public FeatureStatistics() {
        mins = new HashMap<>();
        maxs = new HashMap<>();
        means = new HashMap<>();
        stdevs = new HashMap<>();
    }

    public FeatureStatistics(FeaturePipelineContext[] contexts) {
        mins = new HashMap<>();
        maxs = new HashMap<>();
        means = new HashMap<>();
        stdevs = new HashMap<>();

        elementaryKeys = new HashSet<>();
        globalKeys = new HashSet<>();
        for (FeaturePipelineContext context : contexts) {
            if (context == null) continue;
            elementaryKeys.addAll(context.getElementaryKeys());
            globalKeys.addAll(context.getGlobalKeys());
        }

        for (String key : elementaryKeys) {
            if (!mins.containsKey(key)) mins.put(key, Float.MAX_VALUE);
            if (!maxs.containsKey(key)) maxs.put(key, -Float.MAX_VALUE);
            if (!means.containsKey(key)) means.put(key, 0.0f);
            if (!stdevs.containsKey(key)) stdevs.put(key, 0.0f);
        }

        for (String key : globalKeys) {
            for (String df : Helpers.DISTANCE_FUNCTIONS) {
                if (!means.containsKey(key + df)) means.put(key + df, 0.0f);
                if (!stdevs.containsKey(key + df)) stdevs.put(key + df, 0.0f);
            }
        }

        extractElementaries(contexts);
        extractGlobals(contexts);
    }

    public void loadJson() {
        File json = new File(statisticsFile);
        if (!json.isFile()) throw new IllegalStateException(statisticsFile + " does not exist, please run the pipeline first");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = root.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String key = entry.getKey();
            JsonNode keyNode = entry.getValue();

            processNode(key, keyNode);
        }
    }

    public float getMin(String key) {
        return mins.get(key);
    }

    public float getMax(String key) {
        return maxs.get(key);
    }

    public float getMean(String key) {
        return means.get(key);
    }

    public float getStdev(String key) {
        return stdevs.get(key);
    }

    public void saveToJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        for (String key : elementaryKeys) {
            ObjectNode keyNode = mapper.createObjectNode();
            keyNode.put("min", mins.get(key));
            keyNode.put("max", maxs.get(key));
            keyNode.put("mean", means.get(key));
            keyNode.put("stdev", stdevs.get(key));
            root.set(key, keyNode);
        }

        for (String key : globalKeys) {
            ObjectNode keyNode = mapper.createObjectNode();

            for (String df : Helpers.DISTANCE_FUNCTIONS) {
                String mkey = key + df;

                ObjectNode dfNode = mapper.createObjectNode();
                dfNode.put("mean", means.get(mkey));
                dfNode.put("stdev", stdevs.get(mkey));
                keyNode.set(df, dfNode);
            }

            root.set(key, keyNode);
        }

        try {
            File json = new File(statisticsFile);

            if (!json.isFile() && !json.createNewFile()) throw new IOException("Failed to create new file for " + statisticsFile);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(json, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractElementaries(FeaturePipelineContext[] contexts) {
        Map<String, Float> ns = new HashMap<>();
        for (String key : elementaryKeys) {
            ns.put(key, 0.0f);

            for (FeaturePipelineContext context : contexts) {
                if (context == null) continue;

                float v = context.getElementary(key);
                if (v < mins.get(key)) mins.put(key, v);
                if (v < maxs.get(key)) maxs.put(key, v);

                ns.put(key, ns.get(key) + 1.0f);
                float currMean = means.get(key);
                float delta = v - currMean;
                currMean += delta / ns.get(key);
                means.put(key, currMean);

                float currStdev = stdevs.get(key);
                currStdev += delta * delta;
                stdevs.put(key, currStdev);
            }
        }

        for (String key : elementaryKeys) {
            float variance = stdevs.get(key) / (ns.get(key) - 1.0f);
            float stdev = (float) Math.sqrt(variance);
            stdevs.put(key, stdev);
        }
    }

    private void extractGlobals(FeaturePipelineContext[] contexts) {
        Map<String, Float> ns = new HashMap<>();
        for (String key : globalKeys) {
            ns.put(key, 0.0f);

            for (int i = 0; i < contexts.length; i++) {
                FeaturePipelineContext context1 = contexts[i];
                if (context1 == null) continue;

                for (int j = i + 1; j < contexts.length; j++) {
                    FeaturePipelineContext context2 = contexts[j];
                    if (context2 == null) continue;

                    ns.put(key, ns.get(key) + 1.0f);
                    for (String df : Helpers.DISTANCE_FUNCTIONS) {
                        String mkey = key + df;

                        float[] v1 = context1.getGlobal(key);
                        float[] v2 = context2.getGlobal(key);
                        float distance = Helpers.getDistance(df, v1, v2);

                        float delta = distance - means.get(mkey);
                        means.put(mkey, means.get(mkey) + (delta / ns.get(key)));
                        stdevs.put(mkey, stdevs.get(mkey) + (delta * delta));
                    }
                }
            }
        }

        for (String key : globalKeys) {
            for (String df : Helpers.DISTANCE_FUNCTIONS) {
                String mkey = key + df;

                float variance = stdevs.get(mkey) / (ns.get(key) - 1.0f);
                float stdev = (float) Math.sqrt(variance);
                stdevs.put(mkey, stdev);
            }
        }
    }

    private void processNode(String key, JsonNode node) {
        if (node.has("min")) mins.put(key, node.get("min").floatValue());
        if (node.has("max")) maxs.put(key, node.get("max").floatValue());
        if (node.has("mean")) means.put(key, node.get("mean").floatValue());
        if (node.has("stdev")) stdevs.put(key, node.get("stdev").floatValue());

        for (String df : Helpers.DISTANCE_FUNCTIONS) {
            if (node.has(df)) processNode(key + df, node.get(df));
        }
    }
}
